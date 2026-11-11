package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.HayatimRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "AuthViewModel"

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val _uiState = MutableStateFlow<AuthUiState>(
        if (auth.currentUser != null) AuthUiState.Authenticated(auth.currentUser!!) else AuthUiState.Idle
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.value = if (user != null) {
                AuthUiState.Authenticated(user)
            } else {
                AuthUiState.Idle
            }
        }
    }

    /**
     * Silent Auto-Sign-In on startup with GetGoogleIdOption.
     */
    fun attemptAutoSignIn(context: Context) {
        if (auth.currentUser != null) {
            _uiState.value = AuthUiState.Authenticated(auth.currentUser!!)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id string resource not found", e)
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                handleCredentialResult(result.credential, context)
            } catch (e: Exception) {
                // Silent check failure is expected on first launch, leave Idle for user to click button
                Log.d(TAG, "Auto sign-in bypassed or no stored credentials: ${e.message}")
            }
        }
    }

    /**
     * Interactive Sign-In via GetSignInWithGoogleOption.
     */
    fun onGoogleSignInClicked(activity: Activity) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Google Sign-In yapılandırması eksik (Web Client ID bulunamadı).")
            return
        }

        _uiState.value = AuthUiState.Loading

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        val credentialManager = CredentialManager.create(activity)
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                handleCredentialResult(result.credential, activity)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _uiState.value = AuthUiState.Error(
                    e.localizedMessage ?: "Google ile giriş yapılırken bir sorun oluştu."
                )
            }
        }
    }

    private suspend fun handleCredentialResult(credential: androidx.credentials.Credential, context: Context) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()
            val user = authResult.user
            if (user != null) {
                _uiState.value = AuthUiState.Authenticated(user)
                // Bootstrap User Profile in Firestore if needed
                bootstrapUserProfile(user, context)
            } else {
                _uiState.value = AuthUiState.Error("Kullanıcı kimliği doğrulanamadı.")
            }
        } else {
            _uiState.value = AuthUiState.Error("Beklenmeyen kimlik bilgisi tipi.")
        }
    }

    private fun bootstrapUserProfile(user: FirebaseUser, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repo = HayatimRepository(context)
                val initialProfile = UserProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: "Değerli Kullanıcı",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    isPro = false,
                    monthlyBudget = 15000.0
                )
                repo.saveUserProfile(initialProfile)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to bootstrap initial user profile: ${e.message}")
            }
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    auth.signOut()
                    _uiState.value = AuthUiState.Idle
                }
            }
        }
    }
}
