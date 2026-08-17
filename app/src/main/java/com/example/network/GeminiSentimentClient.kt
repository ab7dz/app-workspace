package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.model.SentimentType
import com.example.model.SocialPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiSentimentClient {

    private const val TAG = "GeminiSentiment"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Batch sentiment analysis on a list of posts
     */
    suspend fun analyzeBatchSentiment(
        topic: String,
        posts: List<SocialPost>,
        customGeminiKey: String? = null
    ): List<SocialPost> = withContext(Dispatchers.IO) {
        if (posts.isEmpty()) return@withContext emptyList()

        val apiKey = if (!customGeminiKey.isNullOrBlank()) customGeminiKey else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key not configured, using smart heuristic sentiment")
            return@withContext fallbackSentiment(posts)
        }

        try {
            val snippetsPayload = posts.mapIndexed { idx, p ->
                "[$idx] منصة: ${p.platform.displayName} | المؤلف: ${p.authorName} | النص: ${p.title} - ${p.snippet}"
            }.joinToString("\n")

            val prompt = """
                أنت خبير استماع اجتماعي (Social Listening) وتحليل مشاعر لبيانات السوشيال ميديا وأبحاث الـ OSINT.
                الموضوع المبحوث عنه: "$topic"
                
                قم بتحليل قائمة المنشورات التالية وحدد لكل منشور:
                1. المشاعر: (POSITIVE أو NEGATIVE أو NEUTRAL)
                2. درجة الثقة: رقم بين 0.5 و 1.0
                3. سبب مقتضب باللغة العربية (جملة واحدة توضح السياق)
                
                أجب بتنسيق JSON حصري بدون أي نصوص إضافية:
                [
                  {
                    "index": 0,
                    "sentiment": "POSITIVE",
                    "confidence": 0.95,
                    "reason": "إشادة قوية بالأداء والنتائج"
                  }
                ]
                
                المنشورات:
                $snippetsPayload
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }.toString()

            val request = Request.Builder()
                .url("$GEMINI_URL?key=$apiKey")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "Gemini API call returned status code: ${response.code}")
                return@withContext fallbackSentiment(posts)
            }

            parseGeminiSentimentResponse(responseBody, posts)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing sentiment with Gemini", e)
            fallbackSentiment(posts)
        }
    }

    /**
     * Generates an Executive OSINT / Marketing summary for the topic
     */
    suspend fun generateExecutiveSummary(
        topic: String,
        posts: List<SocialPost>,
        customGeminiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (posts.isEmpty()) return@withContext "لا توجد منشورات متاحة للتلخيص."

        val apiKey = if (!customGeminiKey.isNullOrBlank()) customGeminiKey else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateHeuristicSummary(topic, posts)
        }

        try {
            val topSnippets = posts.take(12).mapIndexed { idx, p ->
                "${idx + 1}. [${p.platform.displayName}] ${p.title}: ${p.snippet}"
            }.joinToString("\n")

            val prompt = """
                أنت محلل استخبارات مفتوحة المصدر (OSINT) ومستشار استماع اجتماعي (Social Listening) متخصص.
                الموضوع المبحوث عنه: "$topic"
                
                بناءً على عينة المنشورات التالية المجمعة من فيسبوك وإكس وشبكات التواصل:
                $topSnippets
                
                اكتب تقريراً استراتيجياً مقتضباً من 3 إلى 4 نقاط باللغة العربية:
                1. ملخص التوجه العام والرأي العام (Public Sentiment)
                2. أبرز محاور النقاش المشتركة بين مستخدمي فيسبوك وإكس
                3. رؤية تحليلية وتوصية للمسوقين / الصحفيين / الباحثين
                
                اجعل النبرة احترافية، تحليلية ومباشرة.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }.toString()

            val request = Request.Builder()
                .url("$GEMINI_URL?key=$apiKey")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val extractedText = extractTextFromGeminiResponse(responseBody)
                if (!extractedText.isNullOrBlank()) {
                    extractedText
                } else {
                    generateHeuristicSummary(topic, posts)
                }
            } else {
                generateHeuristicSummary(topic, posts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating executive summary", e)
            generateHeuristicSummary(topic, posts)
        }
    }

    private fun generateHeuristicSummary(topic: String, posts: List<SocialPost>): String {
        val pos = posts.count { it.sentiment == SentimentType.POSITIVE }
        val neg = posts.count { it.sentiment == SentimentType.NEGATIVE }
        val neu = posts.count { it.sentiment == SentimentType.NEUTRAL }
        val dominant = when {
            pos > neg && pos >= neu -> "إيجابي بشكل ملحوظ مع تفاعل واعد"
            neg > pos && neg >= neu -> "مائل للتحفظ والنقد البنّاء"
            else -> "متوازن ومحايد مع تبادل مكثف للآراء"
        }
        return """
            📊 ملخص استماع اجتماعي ذكي حول: "$topic"
            • التوجه العام: انطباع $dominant بناءً على تحليل ${posts.size} منشوراً مجمّعاً من منصات فيسبوك وإكس.
            • توزيع المشاعر: $pos إيجابي، $neg سلبي، $neu محايد.
            • التوصية الاستراتيجية (OSINT/Marketing): تعزيز المتابعة الميدانية للكلمات المفتاحية النشطة واستثمار الزخم الحالي في التواصل الرقمي.
        """.trimIndent()
    }

    private fun parseGeminiSentimentResponse(rawJson: String, originalPosts: List<SocialPost>): List<SocialPost> {
        val textContent = extractTextFromGeminiResponse(rawJson)
        if (textContent.isNullOrBlank()) {
            return fallbackSentiment(originalPosts)
        }

        val sentimentMap = mutableMapOf<Int, Triple<SentimentType, Float, String>>()

        try {
            // Find JSON array bounds
            val firstBracket = textContent.indexOf('[')
            val lastBracket = textContent.lastIndexOf(']')

            if (firstBracket != -1 && lastBracket > firstBracket) {
                val arrayStr = textContent.substring(firstBracket, lastBracket + 1)
                val jsonArray = JSONArray(arrayStr)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(i) ?: continue
                    val idx = item.optInt("index", i)
                    val sentStr = item.optString("sentiment", "NEUTRAL")
                    val conf = item.optDouble("confidence", 0.85).toFloat()
                    val reason = item.optString("reason", "تحليل سياقي بالذكاء الاصطناعي")
                    sentimentMap[idx] = Triple(SentimentType.fromString(sentStr), conf, reason)
                }
            } else {
                // Check if wrapped in object with list property
                val firstBrace = textContent.indexOf('{')
                val lastBrace = textContent.lastIndexOf('}')
                if (firstBrace != -1 && lastBrace > firstBrace) {
                    val objStr = textContent.substring(firstBrace, lastBrace + 1)
                    val jsonObj = JSONObject(objStr)
                    val jsonArray = jsonObj.optJSONArray("results")
                        ?: jsonObj.optJSONArray("posts")
                        ?: jsonObj.optJSONArray("items")
                        ?: jsonObj.optJSONArray("sentiments")

                    if (jsonArray != null) {
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.optJSONObject(i) ?: continue
                            val idx = item.optInt("index", i)
                            val sentStr = item.optString("sentiment", "NEUTRAL")
                            val conf = item.optDouble("confidence", 0.85).toFloat()
                            val reason = item.optString("reason", "تحليل سياقي بالذكاء الاصطناعي")
                            sentimentMap[idx] = Triple(SentimentType.fromString(sentStr), conf, reason)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Non-fatal: could not parse full JSON array, falling back safely: ${e.message}")
        }

        if (sentimentMap.isEmpty()) {
            return fallbackSentiment(originalPosts)
        }

        return originalPosts.mapIndexed { index, post ->
            val aiData = sentimentMap[index]
            if (aiData != null) {
                post.copy(
                    sentiment = aiData.first,
                    sentimentConfidence = aiData.second,
                    sentimentReason = aiData.third
                )
            } else {
                val fallbackSent = SocialDataParser.heuristicSentiment(post.snippet + " " + post.title)
                post.copy(
                    sentiment = fallbackSent,
                    sentimentConfidence = 0.80f,
                    sentimentReason = "تحليل لغوي دلالي"
                )
            }
        }
    }

    private fun extractTextFromGeminiResponse(rawJson: String): String? {
        if (rawJson.isBlank()) return null
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val firstPart = parts.optJSONObject(0) ?: return null
            val text = firstPart.optString("text", "")
            if (text.isBlank()) null else text
        } catch (e: Exception) {
            null
        }
    }

    private fun fallbackSentiment(posts: List<SocialPost>): List<SocialPost> {
        return posts.map {
            it.copy(
                sentiment = SocialDataParser.heuristicSentiment(it.snippet + " " + it.title),
                sentimentConfidence = 0.85f,
                sentimentReason = "تحليل معجمي ودلالي فوري"
            )
        }
    }
}
