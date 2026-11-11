package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class HayatimRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createTask_validPayload_createsDocumentAndReturnsId() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HayatimRepository(firestore)

        val task = TaskItem(
            userId = uid,
            title = "Test Görevi",
            description = "Test açıklaması",
            category = "İş",
            priority = "HIGH",
            dueDate = "2026-11-15",
            isCompleted = false
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addTask(task) }
        assertTrue(result.isSuccess)
        val taskId = result.getOrThrow()
        assertTrue(taskId.isNotEmpty())
    }

    @Test
    fun observeTasks_authenticatedUser_receivesCreatedTask() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HayatimRepository(firestore)

        val title = "Unique Task " + UUID.randomUUID().toString()
        val task = TaskItem(
            userId = uid,
            title = title,
            isCompleted = false
        )
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addTask(task) }
        assertTrue(createResult.isSuccess)
        val taskId = createResult.getOrThrow()

        val tasks = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeTasks().first { list -> list.any { it.id == taskId } }
        }
        assertTrue(tasks.any { it.id == taskId && it.title == title })
    }

    @Test
    fun crossUserAccess_bobCannotAccessAliceTasks() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = HayatimRepository(firestore)

        val task = TaskItem(
            userId = aliceUid,
            title = "Gizli Görev",
            isCompleted = false
        )
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.addTask(task).getOrThrow() }

        // Switch to Bob
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("tasks").document(taskId).get().await()
            }
            fail("Expected PERMISSION_DENIED but Bob was able to read Alice's task")
        } catch (e: Exception) {
            val root = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException in cause", root)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, root!!.code)
        }
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = HayatimRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addTask(TaskItem(title = "Unauth", isCompleted = false))
        }
        assertTrue("Expected failure for unauthenticated call", result.isFailure)
    }

    @Test
    fun addTransaction_validPayload_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HayatimRepository(firestore)

        val tx = TransactionItem(
            userId = uid,
            title = "Kira Ödemesi",
            amount = 12000.0,
            type = "EXPENSE",
            category = "Kira",
            date = "2026-11-01"
        )
        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addTransaction(tx) }
        assertTrue(result.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_rules@test.com"
        const val BOB_EMAIL = "bob_rules@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
    }
}
