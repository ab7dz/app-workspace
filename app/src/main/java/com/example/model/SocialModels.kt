package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class PlatformType(
    val id: String,
    val displayName: String,
    val displayNameAr: String,
    val domain: String,
    val brandColorHex: Long
) {
    FACEBOOK("facebook", "Facebook", "فيسبوك", "facebook.com", 0xFF1877F2),
    X_TWITTER("x_twitter", "X (Twitter)", "إكس (تويتر)", "x.com", 0xFF0F172A),
    INSTAGRAM("instagram", "Instagram", "إنستغرام", "instagram.com", 0xFFE1306C),
    REDDIT("reddit", "Reddit", "ريديت", "reddit.com", 0xFFFF4500),
    LINKEDIN("linkedin", "LinkedIn", "لينكد إن", "linkedin.com", 0xFF0A66C2),
    YOUTUBE("youtube", "YouTube", "يوتيوب", "youtube.com", 0xFFFF0000),
    OTHER("other", "Web", "الويب العام", "", 0xFF64748B);

    companion object {
        fun fromUrl(url: String): PlatformType {
            val lower = url.lowercase()
            return when {
                lower.contains("facebook.com") || lower.contains("fb.com") || lower.contains("fb.watch") -> FACEBOOK
                lower.contains("x.com") || lower.contains("twitter.com") || lower.contains("t.co") -> X_TWITTER
                lower.contains("instagram.com") || lower.contains("instagr.am") -> INSTAGRAM
                lower.contains("reddit.com") -> REDDIT
                lower.contains("linkedin.com") -> LINKEDIN
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                else -> OTHER
            }
        }
    }
}

enum class SentimentType(
    val labelEn: String,
    val labelAr: String,
    val colorHex: Long,
    val emoji: String
) {
    POSITIVE("Positive", "إيجابي", 0xFF10B981, "🟢"),
    NEGATIVE("Negative", "سلبي", 0xFFEF4444, "🔴"),
    NEUTRAL("Neutral", "محايد", 0xFF64748B, "⚪"),
    UNKNOWN("Pending", "قيد التحليل", 0xFF94A3B8, "⏳");

    companion object {
        fun fromString(text: String?): SentimentType {
            if (text == null) return UNKNOWN
            val lower = text.lowercase().trim()
            return when {
                lower.contains("pos") || lower.contains("إيجاب") -> POSITIVE
                lower.contains("neg") || lower.contains("سلب") -> NEGATIVE
                lower.contains("neu") || lower.contains("محايد") -> NEUTRAL
                else -> NEUTRAL
            }
        }
    }
}

enum class TimeRangeFilter(val labelAr: String, val labelEn: String, val googleParam: String?) {
    ALL_TIME("كل الأوقات", "All Time", null),
    LAST_24_HOURS("آخر 24 ساعة", "Past 24 Hours", "d1"),
    LAST_WEEK("آخر أسبوع", "Past Week", "w1"),
    LAST_MONTH("آخر شهر", "Past Month", "m1"),
    LAST_YEAR("آخر سنة", "Past Year", "y1")
}

data class SocialPost(
    val id: String,
    val platform: PlatformType,
    val title: String,
    val snippet: String,
    val postUrl: String,
    val cleanUrl: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val publishedTime: String? = null,
    val engagementLikes: Int = 0,
    val engagementComments: Int = 0,
    val engagementShares: Int = 0,
    val sentiment: SentimentType = SentimentType.UNKNOWN,
    val sentimentConfidence: Float = 0.0f,
    val sentimentReason: String? = null,
    val isBookmarked: Boolean = false,
    val extractedHashtags: List<String> = emptyList(),
    val isDuplicateRemoved: Boolean = false
)

data class DorkQuery(
    val keyword: String,
    val selectedPlatforms: Set<PlatformType> = setOf(PlatformType.FACEBOOK, PlatformType.X_TWITTER),
    val timeRange: TimeRangeFilter = TimeRangeFilter.ALL_TIME,
    val exactPhrase: String = "",
    val mustInclude: String = "",
    val excludeTerms: String = "",
    val language: String = "ar"
) {
    fun buildGoogleDork(): String {
        val parts = mutableListOf<String>()

        if (keyword.isNotBlank()) {
            parts.add(keyword.trim())
        }

        if (exactPhrase.isNotBlank()) {
            parts.add("\"${exactPhrase.trim()}\"")
        }

        if (mustInclude.isNotBlank()) {
            mustInclude.split(" ").filter { it.isNotBlank() }.forEach {
                parts.add("+$it")
            }
        }

        if (excludeTerms.isNotBlank()) {
            excludeTerms.split(" ").filter { it.isNotBlank() }.forEach {
                parts.add("-$it")
            }
        }

        // Platform site: filters
        if (selectedPlatforms.isNotEmpty()) {
            val siteFilters = selectedPlatforms.flatMap { platform ->
                when (platform) {
                    PlatformType.FACEBOOK -> listOf("site:facebook.com")
                    PlatformType.X_TWITTER -> listOf("site:x.com", "site:twitter.com")
                    PlatformType.INSTAGRAM -> listOf("site:instagram.com")
                    PlatformType.REDDIT -> listOf("site:reddit.com")
                    PlatformType.LINKEDIN -> listOf("site:linkedin.com")
                    PlatformType.YOUTUBE -> listOf("site:youtube.com")
                    PlatformType.OTHER -> emptyList()
                }
            }

            if (siteFilters.isNotEmpty()) {
                val joined = siteFilters.joinToString(" OR ")
                parts.add("($joined)")
            }
        }

        return parts.joinToString(" ")
    }
}

data class AggregatorStats(
    val totalResults: Int = 0,
    val facebookCount: Int = 0,
    val xCount: Int = 0,
    val otherCount: Int = 0,
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val neutralCount: Int = 0,
    val duplicateCountRemoved: Int = 0,
    val aiExecutiveSummary: String? = null,
    val topTopics: List<String> = emptyList()
) {
    val facebookPercentage: Float
        get() = if (totalResults > 0) (facebookCount.toFloat() / totalResults) * 100 else 0f

    val xPercentage: Float
        get() = if (totalResults > 0) (xCount.toFloat() / totalResults) * 100 else 0f

    val positivePercentage: Float
        get() = if (totalResults > 0) (positiveCount.toFloat() / totalResults) * 100 else 0f

    val negativePercentage: Float
        get() = if (totalResults > 0) (negativeCount.toFloat() / totalResults) * 100 else 0f

    val neutralPercentage: Float
        get() = if (totalResults > 0) (neutralCount.toFloat() / totalResults) * 100 else 0f
}
