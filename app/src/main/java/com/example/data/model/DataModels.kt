package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isPro: Boolean = false,
    val monthlyBudget: Double = 15000.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "displayName" to displayName,
        "email" to email,
        "photoUrl" to photoUrl,
        "isPro" to isPro,
        "monthlyBudget" to monthlyBudget,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Genel",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val dueDate: String = "", // YYYY-MM-DD
    val dueTime: String = "", // HH:mm
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "description" to description,
        "category" to category,
        "priority" to priority,
        "dueDate" to dueDate,
        "dueTime" to dueTime,
        "isCompleted" to isCompleted,
        "reminderEnabled" to reminderEnabled,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class TransactionItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // INCOME, EXPENSE, DEBT, RECEIVABLE
    val category: String = "Diğer",
    val date: String = "", // YYYY-MM-DD
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "amount" to amount,
        "type" to type,
        "category" to category,
        "date" to date,
        "note" to note,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class NoteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Genel",
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "content" to content,
        "category" to category,
        "isFavorite" to isFavorite,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class JobApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val company: String = "",
    val position: String = "",
    val status: String = "APPLIED", // APPLIED, INTERVIEW, OFFER, REJECTED, SAVED
    val appliedDate: String = "",
    val interviewDate: String = "",
    val salaryExpectation: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "company" to company,
        "position" to position,
        "status" to status,
        "appliedDate" to appliedDate,
        "interviewDate" to interviewDate,
        "salaryExpectation" to salaryExpectation,
        "notes" to notes,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class UserDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Genel",
    val ocrText: String = "",
    val notes: String = "",
    val isPrivate: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "category" to category,
        "ocrText" to ocrText,
        "notes" to notes,
        "isPrivate" to isPrivate,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user", // user, model
    val content: String = "",
    val suggestedAction: String? = null, // e.g. "CREATE_TASK", "CREATE_TRANSACTION"
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "role" to role,
        "content" to content,
        "suggestedAction" to suggestedAction,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp())
    ).filterValues { it != null }
}@IgnoreExtraProperties
data class HabitItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val frequency: String = "DAILY", // DAILY, WEEKLY
    val streakCount: Long = 0,
    val lastCompletedDate: String = "", // YYYY-MM-DD
    val targetDaysPerWeek: Long = 7,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "frequency" to frequency,
        "streakCount" to streakCount,
        "lastCompletedDate" to lastCompletedDate,
        "targetDaysPerWeek" to targetDaysPerWeek,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class GoalItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val targetDate: String = "", // YYYY-MM-DD
    val progressPercentage: Long = 0, // 0..100
    val category: String = "Kişisel",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "title" to title,
        "description" to description,
        "targetDate" to targetDate,
        "progressPercentage" to progressPercentage,
        "category" to category,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class AiSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val assistantName: String = "HAYATIM AI",
    val persona: String = "PROFESSIONAL", // PROFESSIONAL, FRIEND, CAREER, FINANCIAL, CREATIVE, TECH, EDUCATION
    val tone: String = "FORMAL", // FORMAL, CASUAL
    val verbosity: String = "DETAILED", // CONCISE, DETAILED
    val voiceSpeed: Float = 1.0f,
    val autoSpeak: Boolean = false,
    val autoDetectActions: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "assistantName" to assistantName,
        "persona" to persona,
        "tone" to tone,
        "verbosity" to verbosity,
        "voiceSpeed" to voiceSpeed,
        "autoSpeak" to autoSpeak,
        "autoDetectActions" to autoDetectActions,
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class AgentActionLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actionType: String = "", // CREATE_TASK, CREATE_TRANSACTION, CREATE_HABIT, ANALYZE_DOCUMENT
    val summary: String = "",
    val status: String = "EXECUTED", // PENDING, EXECUTED, CANCELLED
    val timestamp: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "actionType" to actionType,
        "summary" to summary,
        "status" to status,
        "timestamp" to timestamp,
        "createdAt" to (createdAt ?: FieldValue.serverTimestamp())
    ).filterValues { it != null }
}
