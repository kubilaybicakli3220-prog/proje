package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

import android.graphics.Bitmap
import com.google.firebase.ai.type.content

private const val TAG = "AiAssistantRepo"

data class ExtractedTask(
    val title: String,
    val category: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String
)

data class ExtractedTransaction(
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String
)

data class AiActionProposal(
    val actionType: String, // CREATE_TASK, CREATE_TRANSACTION, CREATE_HABIT, PLAN_DAY, ANALYZE_DOCUMENT
    val title: String,
    val details: String,
    val amount: Double = 0.0,
    val category: String = "Genel",
    val date: String = "",
    val time: String = "",
    val priority: String = "MEDIUM"
)

data class LifeIntelligenceReport(
    val summary: String,
    val productivityScore: Int,
    val financialHealth: String,
    val habitConsistency: String,
    val keyRecommendations: List<String>
)

class AiAssistantRepository {

    // Lazy initialization to avoid premature evaluation during startup
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = com.google.firebase.ai.type.content {
                text(
                    """
                    Sen HAYATIM AI uygulamasının resmi, dünya standartlarında akıllı kişisel yaşam asistanısın.
                    Kullanıcıya daima Türkçe, saygılı, samimi, teşvik edici ve net yanıtlar ver.
                    Görev planlama, bütçe ve harcama yönetimi, notlar, kariyer/CV tavsiyeleri ve günlük yaşam organizasyonunda uzman bir rehbersin.
                    Kullanıcı bir hatırlatıcı, görev veya harcama kaydetmek istediğinde hem yardımcı ol hem de ayrıntıları netleştir.
                    Asla kullanıcıyı yanıltma veya gerçekte olmayan finansal yatırımlar tavsiye etme.
                    """.trimIndent()
                )
            }
        )
    }

    private val taskExtractorModel by lazy {
        val taskSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Görevin kısa ve net başlığı"),
                "category" to Schema.string(description = "Görevin kategorisi: İş, Kişisel, Sağlık, Eğitim, Alışveriş veya Genel"),
                "priority" to Schema.string(description = "LOW, MEDIUM, HIGH veya URGENT"),
                "dueDate" to Schema.string(description = "Son tarih YYYY-MM-DD formatında (bilinmiyorsa bugün veya boş)"),
                "dueTime" to Schema.string(description = "Saat HH:mm formatında (örneğin 10:00)")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = taskSchema
            }
        )
    }

    /**
     * Streams conversational response for the personal assistant chat.
     */
    fun streamChatResponse(userPrompt: String, historyContext: String = ""): Flow<String> = flow {
        val fullPrompt = if (historyContext.isNotBlank()) {
            "Önceki konuşma bağlamı:\n$historyContext\n\nKullanıcı: $userPrompt"
        } else {
            userPrompt
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                generalModel.generateContentStream(fullPrompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI streaming timed out", e)
            emit("Yapay zekâ yanıtı zaman aşımına uğradı. Lütfen tekrar deneyiniz.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI streaming failed", e)
            emit("Yapay zekâ asistanı yanıt üretirken bir hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata"}")
        }
    }

    /**
     * Natural Language Task Parsing (Extracts structured Task from text).
     */
    suspend fun extractTaskFromText(userText: String, todayDate: String): ExtractedTask? {
        return try {
            withTimeout(45_000L) {
                val prompt = "Bugünün tarihi: $todayDate. Kullanıcı ifadesi: \"$userText\". Bu ifadeden görev ayrıntılarını çıkar."
                val response = taskExtractorModel.generateContent(prompt)
                val jsonStr = response.text ?: return@withTimeout null
                val json = JSONObject(jsonStr)
                ExtractedTask(
                    title = json.optString("title", userText),
                    category = json.optString("category", "Genel"),
                    priority = json.optString("priority", "MEDIUM"),
                    dueDate = json.optString("dueDate", todayDate),
                    dueTime = json.optString("dueTime", "")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract task from text", e)
            null
        }
    }

    /**
     * Expense Analysis (Generates smart budget advice based on transactions).
     */
    suspend fun analyzeExpenses(
        transactionsSummary: String,
        monthlyBudget: Double,
        currentSpent: Double
    ): String {
        return try {
            withTimeout(50_000L) {
                val prompt = """
                Kullanıcının aylık bütçe hedefi: ₺$monthlyBudget
                Şu ana kadar yapılan toplam harcama: ₺$currentSpent
                Son harcama kayıtları:
                $transactionsSummary

                Lütfen harcamaları analiz et:
                1. En çok hangi kategoride harcama yapılmış?
                2. Bütçeyi aşma riski var mı?
                3. Kullanıcıya özel 3 somut tasarruf ve finansal yönetim tavsiyesi ver.
                Yanıtını doğrudan Türkçe, maddeler halinde ve motive edici bir dille hazırla.
                """.trimIndent()
                val response = generalModel.generateContent(prompt)
                response.text ?: "Analiz verisi oluşturulamadı."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Expense analysis failed", e)
            "Harcama analizi yapılırken hata oluştu: ${e.localizedMessage}"
        }
    }

    /**
     * Career / CV Improvement Advice.
     */
    suspend fun analyzeCvOrJob(role: String, userBackground: String): String {
        return try {
            withTimeout(50_000L) {
                val prompt = """
                Hedef Pozisyon / Rol: $role
                Kullanıcının deneyimi / notları: $userBackground

                Lütfen şunları sağla:
                1. CV'de bu rol için öne çıkarılması gereken 4 anahtar yetkinlik ve başarı ifadesi.
                2. Bu pozisyonun iş mülakatında gelebilecek en kritik 3 soru ve nasıl cevaplanması gerektiği.
                3. Başvuruda fark yaratacak 1 profesyonel strateji.
                """.trimIndent()
                val response = generalModel.generateContent(prompt)
                response.text ?: "Kariyer tavsiyeleri oluşturulamadı."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Career advice failed", e)
            "Kariyer analizi sırasında hata oluştu: ${e.localizedMessage}"
        }
    }

    /**
     * Daily plan & productivity generator.
     */
    /**
     * Daily plan & productivity generator.
     */
    suspend fun generateDailyPlan(tasksSummary: String): String {
        return try {
            withTimeout(45_000L) {
                val prompt = """
                Kullanıcının bugünkü görevleri ve planı:
                $tasksSummary

                Lütfen kullanıcı için güne harika başlamasını sağlayacak bir günlük akış planı, 
                zaman yönetimi tekniği (ör. Pomodoro) ve günün motivasyon sözünü hazırla.
                """.trimIndent()
                val response = generalModel.generateContent(prompt)
                response.text ?: "Günün planı hazır."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Daily plan generation failed", e)
            "Günlük plan oluşturulurken bir hata oluştu: ${e.localizedMessage}"
        }
    }

    /**
     * Multimodal Image / Document Analysis with Gemini
     */
    suspend fun analyzeDocumentImage(bitmap: Bitmap, prompt: String): String {
        return try {
            withTimeout(60_000L) {
                val inputContent = content {
                    image(bitmap)
                    text(
                        if (prompt.isNotBlank()) prompt
                        else "Bu görseldeki belgeyi, fişi veya metni ayrıntılı şekilde incele. Metinleri çıkar (OCR), önemli tarihleri, tutarları ve eylem maddelerini özetle."
                    )
                }
                val response = generalModel.generateContent(inputContent)
                response.text ?: "Görsel analizi tamamlandı ancak metin üretilemedi."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Document image analysis failed", e)
            "Görsel analiz edilirken bir hata oluştu: ${e.localizedMessage}"
        }
    }

    /**
     * Autonomous Action Parser: Parses any Turkish user intent into a structured AiActionProposal
     */
    suspend fun parseAutonomousAction(userText: String, todayDate: String): AiActionProposal? {
        return try {
            withTimeout(30_000L) {
                val prompt = """
                Bugünün tarihi: $todayDate
                Kullanıcı ifadesi: "$userText"

                Kullanıcının ifadesini analiz et. Eğer bir görev ekleme, hatırlatıcı, finansal işlem (gelir/gider) veya alışkanlık talebi varsa JSON formatında yanıt ver. 
                Hiçbir işlem yoksa sadece "null" yaz.
                JSON şeması:
                {
                   "actionType": "CREATE_TASK" | "CREATE_TRANSACTION" | "CREATE_HABIT" | "NONE",
                   "title": "İşlemin net başlığı",
                   "details": "Açıklama veya ek not",
                   "amount": 0.0,
                   "category": "Kategori",
                   "date": "YYYY-MM-DD",
                   "time": "HH:mm",
                   "priority": "LOW" | "MEDIUM" | "HIGH" | "URGENT"
                }
                Sadece ham JSON veya null döndür.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                val raw = response.text?.trim() ?: return@withTimeout null
                if (raw.equals("null", ignoreCase = true) || raw.contains("\"actionType\": \"NONE\"")) {
                    return@withTimeout null
                }
                val cleanedJson = raw.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(cleanedJson)
                val type = json.optString("actionType", "NONE")
                if (type == "NONE") return@withTimeout null

                AiActionProposal(
                    actionType = type,
                    title = json.optString("title", userText),
                    details = json.optString("details", ""),
                    amount = json.optDouble("amount", 0.0),
                    category = json.optString("category", "Genel"),
                    date = json.optString("date", todayDate),
                    time = json.optString("time", ""),
                    priority = json.optString("priority", "MEDIUM")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Autonomous action parsing failed", e)
            null
        }
    }

    /**
     * Smart Life Intelligence Generator: Analyzes user's actual life data (tasks, habits, budget, goals)
     */
    suspend fun generateSmartLifeReport(
        tasksCount: Int,
        completedTasksCount: Int,
        habitsCount: Int,
        totalStreak: Long,
        monthlyBudget: Double,
        currentSpent: Double,
        goalsCount: Int
    ): String {
        return try {
            withTimeout(50_000L) {
                val prompt = """
                HAYATIM AI Akıllı Yaşam Analizi Motoru:
                Kullanıcı Verileri:
                - Görevler: Toplam $tasksCount, Tamamlanan: $completedTasksCount
                - Alışkanlıklar: $habitsCount takip ediliyor, En yüksek seri: $totalStreak gün
                - Finans: Aylık bütçe ₺$monthlyBudget, Şu ana kadar harcanan: ₺$currentSpent
                - Vizyon Hedefleri: $goalsCount aktif hedef

                Lütfen kullanıcı için kapsamlı bir "Akıllı Yaşam ve Verimlilik Raporu" oluştur:
                1. 📊 Genel Yaşam ve Zaman Yönetimi Puanı (%100 üzerinden) ve Değerlendirmesi
                2. 🎯 Görev ve Alışkanlık Tutarlılığı
                3. 💰 Finansal Sağlık & Bütçe Disiplini Durumu
                4. 💡 Bu Hafta İçin 3 Somut ve Stratejik Yaşam Tavsiyesi
                Motive edici, samimi, profesyonel ve Türkçe bir tonla sun.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                response.text ?: "Yaşam analizi oluşturuldu."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Life intelligence generation failed", e)
            "Yaşam analizi hazırlanırken bir hata oluştu: ${e.localizedMessage}"
        }
    }

    /**
     * AI App Center: Executes specialized domain-expert assistant modules
     */
    suspend fun runSpecializedAiModule(moduleId: String, moduleTitle: String, userInput: String): String {
        return try {
            withTimeout(50_000L) {
                val systemRole = when (moduleId) {
                    "education" -> "Sen HAYATIM AI Eğitim ve Öğrenim Asistanısın. Konu anlatımı, sınav hazırlığı ve verimli çalışma tekniklerinde uzmansın."
                    "travel" -> "Sen HAYATIM AI Seyahat Asistanısın. Rota çıkarma, gezilecek yerler, bütçe planı ve bavul listesi hazırlamada uzmansın."
                    "shopping" -> "Sen HAYATIM AI Akıllı Alışveriş Danışmanısın. İhtiyaç analizi, ürün karşılaştırması ve bütçe tasarrufunda uzmansın."
                    "career" -> "Sen HAYATIM AI Kariyer Koçusun. CV hazırlama, mülakat simülasyonu ve iş stratejilerinde uzmansın."
                    "finance" -> "Sen HAYATIM AI Finans Danışmanısın. Harcama kontrolü, tasarruf stratejileri ve bütçe disiplininde uzmansın."
                    "real_estate" -> "Sen HAYATIM AI Emlak ve Gayrimenkul Asistanısın. İlan metni yazımı, kira analizi ve mülk yönetiminde uzmansın."
                    "creative" -> "Sen HAYATIM AI Yaratıcı İçerik Asistanısın. Metin yazarlığı, sosyal medya fikirleri ve hikaye kurgusunda uzmansın."
                    "software" -> "Sen HAYATIM AI Yazılım Mühendisi Asistanısın. Kotlin, Android mimarisi, kod optimizasyonu ve hata ayıklamada uzmansın."
                    else -> "Sen HAYATIM AI Uzman Asistanısın."
                }

                val prompt = """
                $systemRole
                Kullanıcı İsteği / Sorusu:
                $userInput

                Lütfen kapsamlı, pratik, doğrudan uygulanabilir ve yüksek kaliteli bir yanıt hazırla.
                """.trimIndent()

                val response = generalModel.generateContent(prompt)
                response.text ?: "Modül yanıtı üretilemedi."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Specialized module run failed", e)
            "İşlem sırasında hata oluştu: ${e.localizedMessage}"
        }
    }
}


