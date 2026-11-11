package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantRepository
import com.example.data.ai.ExtractedTask
import com.example.data.model.AgentActionLog
import com.example.data.model.AiSettings
import com.example.data.model.ChatMessage
import com.example.data.model.JobApplication
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.model.UserDocument
import com.example.data.model.UserProfile
import com.example.data.repository.HayatimRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.graphics.Bitmap
import com.example.data.ai.AiActionProposal
import com.example.data.model.GoalItem
import com.example.data.model.HabitItem
import com.example.util.NotificationHelper
import com.example.util.TextToSpeechManager

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HayatimRepository(application)
    private val aiRepository = AiAssistantRepository()
    private val ttsManager = TextToSpeechManager(application)

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<TaskItem>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitItem>> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalItem>> = repository.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteItem>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<JobApplication>> = repository.observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<UserDocument>> = repository.observeDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.observeChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiSettings: StateFlow<AiSettings> = repository.observeAiSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiSettings())

    val agentActionLogs: StateFlow<List<AgentActionLog>> = repository.observeAgentActionLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lifeReport = MutableStateFlow<String?>(null)
    val lifeReport: StateFlow<String?> = _lifeReport.asStateFlow()

    private val _isGeneratingLifeReport = MutableStateFlow(false)
    val isGeneratingLifeReport: StateFlow<Boolean> = _isGeneratingLifeReport.asStateFlow()

    private val _activeAppModuleResult = MutableStateFlow<String?>(null)
    val activeAppModuleResult: StateFlow<String?> = _activeAppModuleResult.asStateFlow()

    private val _isAppModuleLoading = MutableStateFlow(false)
    val isAppModuleLoading: StateFlow<Boolean> = _isAppModuleLoading.asStateFlow()

    private val _aiStreamingText = MutableStateFlow("")
    val aiStreamingText: StateFlow<String> = _aiStreamingText.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _extractedTask = MutableStateFlow<ExtractedTask?>(null)
    val extractedTask: StateFlow<ExtractedTask?> = _extractedTask.asStateFlow()

    private val _actionProposal = MutableStateFlow<AiActionProposal?>(null)
    val actionProposal: StateFlow<AiActionProposal?> = _actionProposal.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult: StateFlow<String?> = _aiAnalysisResult.asStateFlow()

    private val _isAnalyzingExpenses = MutableStateFlow(false)
    val isAnalyzingExpenses: StateFlow<Boolean> = _isAnalyzingExpenses.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearExtractedTask() {
        _extractedTask.value = null
    }

    // ==================== TASK ACTIONS ====================

    fun addTask(
        title: String,
        description: String = "",
        category: String = "Genel",
        priority: String = "MEDIUM",
        dueDate: String = "",
        dueTime: String = "",
        reminder: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskItem(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                isCompleted = false,
                reminderEnabled = reminder
            )
            repository.addTask(task).onSuccess {
                _statusMessage.value = "Görev başarıyla eklendi!"
            }.onFailure { e ->
                _statusMessage.value = "Görev eklenemedi: ${e.localizedMessage}"
            }
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(taskId)
        }
    }

    // ==================== TRANSACTION ACTIONS ====================

    fun addTransaction(
        title: String,
        amount: Double,
        type: String, // INCOME, EXPENSE, DEBT, RECEIVABLE
        category: String,
        date: String = "",
        note: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = if (date.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else date

            val item = TransactionItem(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                date = dateStr,
                note = note.trim()
            )
            repository.addTransaction(item).onSuccess {
                _statusMessage.value = "İşlem kaydedildi!"
            }.onFailure { e ->
                _statusMessage.value = "İşlem kaydedilemedi: ${e.localizedMessage}"
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transactionId)
        }
    }

    // ==================== NOTE ACTIONS ====================

    fun addNote(
        title: String,
        content: String,
        category: String = "Genel",
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteItem(
                title = title.trim(),
                content = content.trim(),
                category = category,
                isFavorite = isFavorite
            )
            repository.addNote(note).onSuccess {
                _statusMessage.value = "Not kaydedildi!"
            }.onFailure { e ->
                _statusMessage.value = "Not kaydedilemedi: ${e.localizedMessage}"
            }
        }
    }

    fun toggleFavoriteNote(note: NoteItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = note.copy(isFavorite = !note.isFavorite)
            repository.updateNote(updated)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteId)
        }
    }

    // ==================== JOB ACTIONS ====================

    fun addJob(
        company: String,
        position: String,
        status: String = "APPLIED",
        appliedDate: String = "",
        interviewDate: String = "",
        salaryExpectation: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = JobApplication(
                company = company.trim(),
                position = position.trim(),
                status = status,
                appliedDate = appliedDate,
                interviewDate = interviewDate,
                salaryExpectation = salaryExpectation,
                notes = notes.trim()
            )
            repository.addJob(job).onSuccess {
                _statusMessage.value = "İş başvurusu eklendi!"
            }.onFailure { e ->
                _statusMessage.value = "Başvuru eklenemedi: ${e.localizedMessage}"
            }
        }
    }

    fun updateJobStatus(job: JobApplication, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateJob(job.copy(status = newStatus))
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteJob(jobId)
        }
    }

    // ==================== DOCUMENT ACTIONS ====================

    fun addDocument(
        title: String,
        category: String = "Genel",
        ocrText: String = "",
        notes: String = "",
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val doc = UserDocument(
                title = title.trim(),
                category = category,
                ocrText = ocrText.trim(),
                notes = notes.trim(),
                isPrivate = isPrivate
            )
            repository.addDocument(doc).onSuccess {
                _statusMessage.value = "Belge kaydedildi!"
            }.onFailure { e ->
                _statusMessage.value = "Belge kaydedilemedi: ${e.localizedMessage}"
            }
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDocument(documentId)
        }
    }

    // ==================== AI ASSISTANT CHAT & ACTIONS ====================

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            // Save user message to Firestore
            val userMsg = ChatMessage(role = "user", content = userText)
            repository.addChatMessage(userMsg)

            _isAiGenerating.value = true
            _aiStreamingText.value = ""

            // Stream response from Gemini
            val history = chatMessages.value.takeLast(6).joinToString("\n") { "${it.role}: ${it.content}" }
            var finalAiText = ""

            aiRepository.streamChatResponse(userText, history).collect { partialText ->
                _aiStreamingText.value = partialText
                finalAiText = partialText
            }

            // Save completed AI response
            if (finalAiText.isNotBlank()) {
                val modelMsg = ChatMessage(role = "model", content = finalAiText)
                repository.addChatMessage(modelMsg)
                if (aiSettings.value.autoSpeak) {
                    ttsManager.speak(finalAiText)
                }
            }
            _aiStreamingText.value = ""
            _isAiGenerating.value = false

            // Autonomous Action Proposal check
            if (aiSettings.value.autoDetectActions) {
                val proposal = aiRepository.parseAutonomousAction(userText, today)
                if (proposal != null && proposal.title.isNotBlank()) {
                    _actionProposal.value = proposal
                }
            }
        }
    }

    fun dismissActionProposal() {
        _actionProposal.value = null
    }

    fun executeActionProposal(proposal: AiActionProposal) {
        viewModelScope.launch(Dispatchers.IO) {
            when (proposal.actionType) {
                "CREATE_TASK" -> {
                    addTask(
                        title = proposal.title,
                        description = proposal.details,
                        category = proposal.category,
                        priority = proposal.priority,
                        dueDate = proposal.date,
                        dueTime = proposal.time,
                        reminder = true
                    )
                }
                "CREATE_TRANSACTION" -> {
                    addTransaction(
                        title = proposal.title,
                        amount = proposal.amount,
                        type = if (proposal.amount < 0) "EXPENSE" else "EXPENSE",
                        category = proposal.category,
                        date = proposal.date,
                        note = proposal.details
                    )
                }
                "CREATE_HABIT" -> {
                    addHabit(
                        title = proposal.title,
                        frequency = "DAILY",
                        targetDays = 7
                    )
                }
            }

            // Log agent action
            repository.logAgentAction(
                AgentActionLog(
                    actionType = proposal.actionType,
                    summary = "${proposal.title} (${proposal.category})",
                    status = "EXECUTED",
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
            )

            _actionProposal.value = null
            _statusMessage.value = "AI Ajanı işlemi tamamladı: ${proposal.title}"
        }
    }

    fun confirmExtractedTask(task: ExtractedTask) {
        addTask(
            title = task.title,
            category = task.category,
            priority = task.priority,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            reminder = true
        )
        _extractedTask.value = null
        _statusMessage.value = "AI tarafından önerilen görev kaydedildi: ${task.title}"
    }

    fun analyzeExpensesWithAi() {
        val currentBudget = userProfile.value?.monthlyBudget ?: 15000.0
        val txList = transactions.value
        val expenses = txList.filter { it.type == "EXPENSE" }
        val totalSpent = expenses.sumOf { it.amount }

        val summary = expenses.take(15).joinToString("\n") {
            "- ${it.date}: ${it.title} (₺${it.amount}, ${it.category})"
        }

        viewModelScope.launch {
            _isAnalyzingExpenses.value = true
            _aiAnalysisResult.value = null
            val result = aiRepository.analyzeExpenses(
                transactionsSummary = summary.ifBlank { "Henüz kayıtlı harcama bulunmamaktadır." },
                monthlyBudget = currentBudget,
                currentSpent = totalSpent
            )
            _aiAnalysisResult.value = result
            _isAnalyzingExpenses.value = false
        }
    }

    fun clearAiAnalysis() {
        _aiAnalysisResult.value = null
    }

    fun updateMonthlyBudget(newBudget: Double) {
        val profile = userProfile.value ?: UserProfile(
            userId = repository.requireUserId(),
            displayName = "Kullanıcı",
            email = "",
            monthlyBudget = newBudget
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile.copy(monthlyBudget = newBudget)).onSuccess {
                _statusMessage.value = "Aylık bütçe güncellendi: ₺$newBudget"
            }
        }
    }

    // ==================== HABIT ACTIONS ====================

    fun addHabit(title: String, frequency: String = "DAILY", targetDays: Long = 7) {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = HabitItem(
                title = title.trim(),
                frequency = frequency,
                streakCount = 0,
                targetDaysPerWeek = targetDays
            )
            repository.addHabit(habit).onSuccess {
                _statusMessage.value = "Alışkanlık eklendi: $title"
            }.onFailure { e ->
                _statusMessage.value = "Alışkanlık eklenemedi: ${e.localizedMessage}"
            }
        }
    }

    fun completeHabitToday(habit: HabitItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newStreak = if (habit.lastCompletedDate == today) {
                habit.streakCount // already completed today
            } else {
                habit.streakCount + 1
            }
            val updated = habit.copy(
                streakCount = newStreak,
                lastCompletedDate = today
            )
            repository.updateHabit(updated).onSuccess {
                _statusMessage.value = "🔥 Harika! ${habit.title} serisi: $newStreak gün!"
                NotificationHelper.sendNotification(
                    getApplication(),
                    (System.currentTimeMillis() % 100000).toInt(),
                    "🔥 Alışkanlık Tamamlandı!",
                    "${habit.title} tamamlandı. Seriniz $newStreak güne ulaştı!"
                )
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabit(habitId)
        }
    }

    // ==================== GOAL ACTIONS ====================

    fun addGoal(title: String, description: String = "", targetDate: String = "", category: String = "Kişisel") {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = GoalItem(
                title = title.trim(),
                description = description.trim(),
                targetDate = targetDate,
                progressPercentage = 0,
                category = category
            )
            repository.addGoal(goal).onSuccess {
                _statusMessage.value = "Hedef oluşturuldu: $title"
            }.onFailure { e ->
                _statusMessage.value = "Hedef eklenemedi: ${e.localizedMessage}"
            }
        }
    }

    fun updateGoalProgress(goal: GoalItem, progress: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(progressPercentage = progress.coerceIn(0, 100))
            repository.updateGoal(updated).onSuccess {
                _statusMessage.value = "${goal.title} ilerlemesi: %$progress"
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goalId)
        }
    }

    // ==================== TTS & AUDIO ====================

    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    // ==================== MULTIMODAL DOCUMENT ANALYSIS ====================

    fun analyzeDocumentBitmap(bitmap: Bitmap, prompt: String = "") {
        viewModelScope.launch {
            _isAiGenerating.value = true
            _aiStreamingText.value = "Görsel Gemini 3.8 Flash ile taranıyor ve metin çıkarılıyor..."
            val result = aiRepository.analyzeDocumentImage(bitmap, prompt)
            _aiStreamingText.value = ""
            _isAiGenerating.value = false

            // Save as chat message
            repository.addChatMessage(ChatMessage(role = "user", content = "[Görsel / Belge Taraması Yapıldı]"))
            repository.addChatMessage(ChatMessage(role = "model", content = result))
        }
    }

    // ==================== SMART LIFE INTELLIGENCE ====================

    fun generateLifeIntelligenceReport() {
        val tList = tasks.value
        val hList = habits.value
        val gList = goals.value
        val txList = transactions.value

        val tasksCount = tList.size
        val completedCount = tList.count { it.isCompleted }
        val habitsCount = hList.size
        val maxStreak = hList.maxOfOrNull { it.streakCount } ?: 0L
        val budget = userProfile.value?.monthlyBudget ?: 15000.0
        val spent = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val goalsCount = gList.size

        viewModelScope.launch {
            _isGeneratingLifeReport.value = true
            _lifeReport.value = null
            val result = aiRepository.generateSmartLifeReport(
                tasksCount = tasksCount,
                completedTasksCount = completedCount,
                habitsCount = habitsCount,
                totalStreak = maxStreak,
                monthlyBudget = budget,
                currentSpent = spent,
                goalsCount = goalsCount
            )
            _lifeReport.value = result
            _isGeneratingLifeReport.value = false
        }
    }

    fun clearLifeReport() {
        _lifeReport.value = null
    }

    // ==================== AI APP CENTER ====================

    fun runSpecializedAppModule(moduleId: String, moduleTitle: String, input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _isAppModuleLoading.value = true
            _activeAppModuleResult.value = null
            val result = aiRepository.runSpecializedAiModule(moduleId, moduleTitle, input)
            _activeAppModuleResult.value = result
            _isAppModuleLoading.value = false
        }
    }

    fun clearAppModuleResult() {
        _activeAppModuleResult.value = null
    }

    // ==================== AI PERSONALIZATION & SETTINGS ====================

    fun updateAiSettings(settings: AiSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveAiSettings(settings).onSuccess {
                _statusMessage.value = "AI Kişiselleştirme tercihleri güncellendi."
            }
        }
    }

    fun deleteAccountData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserAllData().onSuccess {
                _statusMessage.value = "Tüm verileriniz kalıcı olarak silindi."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
