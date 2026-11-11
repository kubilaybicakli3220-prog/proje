package com.example.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.auth.AuthViewModel
import com.example.ui.home.MainViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TealAccent
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by mainViewModel.userProfile.collectAsState()
    val tasks by mainViewModel.tasks.collectAsState()
    val transactions by mainViewModel.transactions.collectAsState()
    val notes by mainViewModel.notes.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf((userProfile?.monthlyBudget ?: 15000.0).toString()) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showProDialog by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // User Info Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!userProfile?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = userProfile?.photoUrl,
                            contentDescription = "Profil Resmi",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profil",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile?.displayName ?: "Kullanıcı",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (userProfile?.isPro == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PRO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    modifier = Modifier
                                        .background(AmberAccent, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = userProfile?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Pro Subscription Promotion Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showProDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NavyCard
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = AmberAccent.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pro",
                                tint = AmberAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HAYATIM AI PRO",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AmberAccent
                        )
                        Text(
                            text = "Gelişmiş Gemini modelleri, sınırsız analiz & özel şifreli alan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Text(
                        text = "İncele >",
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }
            }
        }

        // Settings & Budget Section
        item {
            Text(
                text = "Kişiselleştirme ve Hedefler",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Aylık Hedef Bütçe",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Şu an: ${currencyFormat.format(userProfile?.monthlyBudget ?: 15000.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent
                            )
                        }

                        IconButton(onClick = { showBudgetDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Bütçe Değiştir", tint = CyanAccent)
                        }
                    }
                }
            }
        }

        // AI Personalization Settings
        item {
            val aiSettings by mainViewModel.aiSettings.collectAsState()
            var showAiSettingsDialog by remember { mutableStateOf(false) }

            Text(
                text = "Yapay Zekâ Kişiselleştirme & Hafıza",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Asistan: ${aiSettings.assistantName} (${aiSettings.persona})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                            Text(
                                text = "Ton: ${if (aiSettings.tone == "FORMAL") "Resmî" else "Samimi"} • ${if (aiSettings.autoSpeak) "Sesli Yanıt Açık" else "Sesli Yanıt Kapalı"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        androidx.compose.material3.TextButton(onClick = { showAiSettingsDialog = true }) {
                            Text("Düzenle", color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Otonom Ajan Algılaması", style = MaterialTheme.typography.bodyMedium)
                        androidx.compose.material3.Switch(
                            checked = aiSettings.autoDetectActions,
                            onCheckedChange = { checked ->
                                mainViewModel.updateAiSettings(aiSettings.copy(autoDetectActions = checked))
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Yanıtlarda Otomatik Seslendirme", style = MaterialTheme.typography.bodyMedium)
                        androidx.compose.material3.Switch(
                            checked = aiSettings.autoSpeak,
                            onCheckedChange = { checked ->
                                mainViewModel.updateAiSettings(aiSettings.copy(autoSpeak = checked))
                            }
                        )
                    }
                }
            }

            if (showAiSettingsDialog) {
                var name by remember { mutableStateOf(aiSettings.assistantName) }
                var persona by remember { mutableStateOf(aiSettings.persona) }
                var tone by remember { mutableStateOf(aiSettings.tone) }

                androidx.compose.ui.window.Dialog(onDismissRequest = { showAiSettingsDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "AI Kişilik & Davranış Ayarları", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Asistan Adı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text("Kişilik / Rol Seçimi:", style = MaterialTheme.typography.labelMedium)
                            val personas = listOf(
                                "PROFESSIONAL" to "Profesyonel Asistan",
                                "FRIEND" to "Günlük Arkadaş",
                                "CAREER" to "Kariyer Danışmanı",
                                "FINANCIAL" to "Finans Koçu",
                                "CREATIVE" to "Yaratıcı Asistan"
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                personas.forEach { (pKey, pLabel) ->
                                    val isSelected = persona == pKey
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().clickable { persona = pKey },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = pLabel,
                                            color = if (isSelected) NavyDark else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(10.dp),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showAiSettingsDialog = false }) { Text("İptal") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    mainViewModel.updateAiSettings(
                                        aiSettings.copy(
                                            assistantName = name.ifBlank { "HAYATIM AI" },
                                            persona = persona,
                                            tone = tone
                                        )
                                    )
                                    showAiSettingsDialog = false
                                }) {
                                    Text("Kaydet")
                                }
                            }
                        }
                    }
                }
            }
        }

        // KVKK and Privacy Policy
        item {
            Text(
                text = "Gizlilik ve Güvenlik (KVKK)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Güvenlik", tint = TealAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "6698 Sayılı KVKK Uyumlu Altyapı",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Verileriniz yalnızca sizin Google hesabınıza ait Firebase Firestore alanında saklanır. Finansal veya kişisel bilgileriniz asla 3. taraflarla paylaşılmaz veya reklam amaçlı kullanılmaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Export data
                    OutlinedButton(
                        onClick = {
                            val exportJson = buildString {
                                appendLine("{")
                                appendLine("  \"kullanici\": \"${userProfile?.email}\",")
                                appendLine("  \"gorev_sayisi\": ${tasks.size},")
                                appendLine("  \"harcama_sayisi\": ${transactions.size},")
                                appendLine("  \"not_sayisi\": ${notes.size}")
                                appendLine("}")
                            }
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, exportJson)
                                putExtra(Intent.EXTRA_TITLE, "hayatim_ai_verilerim.json")
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(intent, "Verilerimi Dışa Aktar"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Dışa Aktar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verilerimi Dışa Aktar (JSON)")
                    }
                }
            }
        }

        // Actions: Sign out & Delete Account
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { authViewModel.signOut(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Oturumu Kapat", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Sil", tint = CoralAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hesabı ve Tüm Verileri Sil", color = CoralAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Budget Dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Aylık Bütçe Belirle") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Tutar (₺)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 15000.0
                        mainViewModel.updateMonthlyBudget(amount)
                        showBudgetDialog = false
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hesap ve Veri Silme Onayı") },
            text = {
                Text("KVKK hakkınız kapsamında tüm görevleriniz, finans hareketleriniz, notlarınız ve belgeleriniz kalıcı olarak silinecektir. Bu işlem geri alınamaz. Devam etmek istiyor musunuz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.deleteAccountData()
                        showDeleteConfirmDialog = false
                        authViewModel.signOut(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                ) {
                    Text("Evet, Her Şeyi Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Pro Showcase Dialog
    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            title = { Text("HAYATIM AI PRO") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PRO Ayrıcalıkları:", fontWeight = FontWeight.Bold)
                    Text("✓ Sınırsız Gemini 3.8 Flash yapay zekâ sohbeti")
                    Text("✓ Gelişmiş bütçe ve harcama içgörüleri")
                    Text("✓ Şifreli belge kasası ve sınırsız OCR")
                    Text("✓ Kariyer & CV hazırlama koçluğu")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Aylık ₺79.99 (Google Play Billing uyumlu)", fontWeight = FontWeight.Bold, color = AmberAccent)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                ) {
                    Text("Tamam", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
