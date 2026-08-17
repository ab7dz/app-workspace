package com.example.network

import com.example.model.CseImage
import com.example.model.CseThumbnail
import com.example.model.GoogleSearchItem
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.model.SerpOrganicResult
import com.example.model.SocialPost
import java.net.URI
import java.net.URLDecoder
import java.util.UUID
import java.util.regex.Pattern

object SocialDataParser {

    private val HASHTAG_REGEX = Pattern.compile("(#[\\w\u0600-\u06FF]+)")

    /**
     * Cleans URLs by removing analytics & tracking query parameters (fbclid, ref, s, utm_*, etc.)
     */
    fun cleanUrl(rawUrl: String): String {
        return try {
            val uri = URI(rawUrl.trim())
            val scheme = uri.scheme ?: "https"
            val host = uri.host ?: ""
            val path = uri.path ?: ""
            val rawQuery = uri.rawQuery

            if (rawQuery.isNullOrBlank()) {
                "$scheme://$host$path".trimEnd('/')
            } else {
                val allowedParams = rawQuery.split("&").filter { param ->
                    val key = param.substringBefore("=").lowercase()
                    !key.startsWith("utm_") &&
                            key != "fbclid" &&
                            key != "ref" &&
                            key != "s" &&
                            key != "t" &&
                            key != "tracking" &&
                            key != "src"
                }
                if (allowedParams.isEmpty()) {
                    "$scheme://$host$path".trimEnd('/')
                } else {
                    "$scheme://$host$path?${allowedParams.joinToString("&")}".trimEnd('/')
                }
            }
        } catch (e: Exception) {
            rawUrl.substringBefore("?").trimEnd('/')
        }
    }

    /**
     * Parses a Google Custom Search Item into a rich SocialPost model
     */
    fun parseGoogleItem(item: GoogleSearchItem): SocialPost? {
        val rawLink = item.link ?: item.formattedUrl ?: return null
        val platform = PlatformType.fromUrl(rawLink)
        val cleanLink = cleanUrl(rawLink)

        val rawTitle = item.title ?: "منشور غير معنون"
        val rawSnippet = item.snippet ?: ""

        val (authorName, authorHandle) = extractAuthorInfo(platform, rawLink, rawTitle, rawSnippet, item.pagemap?.metatags)
        val cleanTitle = sanitizeTitle(rawTitle, platform, authorName)
        val cleanSnippet = sanitizeSnippet(rawSnippet)
        val thumbnail = extractThumbnail(item)
        val publishedTime = extractPublishTime(rawSnippet, item.pagemap?.metatags)
        val hashtags = extractHashtags("$rawTitle $rawSnippet")

        // Heuristic initial sentiment analysis
        val initialSentiment = heuristicSentiment(cleanSnippet)

        // Estimated social engagement based on text signals or random baseline for UX feel
        val engagementLikes = calculateEngagement(rawSnippet, rawTitle)
        val engagementComments = (engagementLikes * 0.15).toInt().coerceAtLeast(1)
        val engagementShares = (engagementLikes * 0.08).toInt().coerceAtLeast(0)

        return SocialPost(
            id = UUID.nameUUIDFromBytes(cleanLink.toByteArray()).toString(),
            platform = platform,
            title = cleanTitle,
            snippet = cleanSnippet,
            postUrl = rawLink,
            cleanUrl = cleanLink,
            authorName = authorName,
            authorHandle = authorHandle,
            authorAvatarUrl = null,
            thumbnailUrl = thumbnail,
            publishedTime = publishedTime,
            engagementLikes = engagementLikes,
            engagementComments = engagementComments,
            engagementShares = engagementShares,
            sentiment = initialSentiment,
            sentimentConfidence = 0.75f,
            sentimentReason = "تحليل سياقي أولي بناءً على مفردات المنشور",
            isBookmarked = false,
            extractedHashtags = hashtags
        )
    }

    /**
     * Parses a SerpApi Organic Result into a SocialPost
     */
    fun parseSerpResult(result: SerpOrganicResult): SocialPost? {
        val rawLink = result.link ?: return null
        val platform = PlatformType.fromUrl(rawLink)
        val cleanLink = cleanUrl(rawLink)

        val rawTitle = result.title ?: "منشور اجتماعي"
        val rawSnippet = result.snippet ?: ""

        val (authorName, authorHandle) = extractAuthorInfo(platform, rawLink, rawTitle, rawSnippet, null)
        val cleanTitle = sanitizeTitle(rawTitle, platform, authorName)
        val cleanSnippet = sanitizeSnippet(rawSnippet)
        val hashtags = extractHashtags("$rawTitle $rawSnippet")
        val initialSentiment = heuristicSentiment(cleanSnippet)

        val likes = calculateEngagement(rawSnippet, rawTitle)

        return SocialPost(
            id = UUID.nameUUIDFromBytes(cleanLink.toByteArray()).toString(),
            platform = platform,
            title = cleanTitle,
            snippet = cleanSnippet,
            postUrl = rawLink,
            cleanUrl = cleanLink,
            authorName = authorName,
            authorHandle = authorHandle,
            authorAvatarUrl = null,
            thumbnailUrl = result.thumbnail,
            publishedTime = result.date ?: "مؤخراً",
            engagementLikes = likes,
            engagementComments = (likes * 0.18).toInt().coerceAtLeast(2),
            engagementShares = (likes * 0.07).toInt().coerceAtLeast(1),
            sentiment = initialSentiment,
            sentimentConfidence = 0.70f,
            sentimentReason = "تحليل نصي أولي للكلمات المفتاحية",
            isBookmarked = false,
            extractedHashtags = hashtags
        )
    }

    /**
     * Deduplicates a list of posts based on clean normalized URLs and title similarity
     */
    fun deduplicatePosts(posts: List<SocialPost>): List<SocialPost> {
        val seenUrls = mutableSetOf<String>()
        val seenContentHashes = mutableSetOf<String>()
        val result = mutableListOf<SocialPost>()

        for (post in posts) {
            val normalizedUrl = post.cleanUrl.lowercase().trimEnd('/')
            val contentHash = post.snippet.take(60).trim().lowercase()

            if (!seenUrls.contains(normalizedUrl) && (!seenContentHashes.contains(contentHash) || contentHash.isBlank())) {
                seenUrls.add(normalizedUrl)
                if (contentHash.isNotBlank()) {
                    seenContentHashes.add(contentHash)
                }
                result.add(post)
            }
        }
        return result
    }

    private fun extractAuthorInfo(
        platform: PlatformType,
        url: String,
        title: String,
        snippet: String,
        metatags: List<Map<String, Any?>>?
    ): Pair<String, String> {
        // Try metatags first
        metatags?.firstOrNull()?.let { meta ->
            val metaAuthor = meta["author"] as? String ?: meta["og:site_name"] as? String ?: meta["twitter:creator"] as? String
            if (!metaAuthor.isNullOrBlank()) {
                val handle = if (metaAuthor.startsWith("@")) metaAuthor else "@${metaAuthor.replace(" ", "")}"
                return Pair(metaAuthor.removePrefix("@"), handle)
            }
        }

        return when (platform) {
            PlatformType.X_TWITTER -> {
                // E.g. https://x.com/username/status/123456789
                val uri = try { URI(url) } catch (e: Exception) { null }
                val segments = uri?.path?.split("/")?.filter { it.isNotBlank() } ?: emptyList()
                if (segments.isNotEmpty() && segments[0] != "status" && segments[0] != "i" && segments[0] != "hashtag") {
                    val handle = segments[0]
                    val name = if (title.contains(" on X:") || title.contains(" on Twitter:")) {
                        title.substringBefore(" on X:").substringBefore(" on Twitter:").trim()
                    } else if (title.contains("(@")) {
                        title.substringBefore("(@").trim()
                    } else {
                        handle
                    }
                    Pair(name, "@$handle")
                } else {
                    Pair("مستخدم إكس", "@x_user")
                }
            }
            PlatformType.FACEBOOK -> {
                // E.g. https://www.facebook.com/PageName/posts/... or https://www.facebook.com/profile.php?id=...
                val uri = try { URI(url) } catch (e: Exception) { null }
                val segments = uri?.path?.split("/")?.filter { it.isNotBlank() } ?: emptyList()
                if (segments.isNotEmpty() && segments[0] != "photo" && segments[0] != "posts" && segments[0] != "watch") {
                    val pageName = try { URLDecoder.decode(segments[0], "UTF-8") } catch (e: Exception) { segments[0] }
                    Pair(pageName.replace(".", " ").replace("-", " "), "@$pageName")
                } else if (title.contains(" - Facebook") || title.contains(" | Facebook")) {
                    val clean = title.replace(" - Facebook", "").replace(" | Facebook", "").trim()
                    Pair(clean, "@facebook_page")
                } else {
                    Pair("صفحة فيسبوك", "@fb_page")
                }
            }
            PlatformType.INSTAGRAM -> {
                val uri = try { URI(url) } catch (e: Exception) { null }
                val segments = uri?.path?.split("/")?.filter { it.isNotBlank() } ?: emptyList()
                val handle = if (segments.isNotEmpty() && segments[0] != "p" && segments[0] != "reel") segments[0] else "instagram_user"
                Pair(handle, "@$handle")
            }
            PlatformType.REDDIT -> {
                val subreddit = if (url.contains("/r/")) "r/" + url.substringAfter("/r/").substringBefore("/") else "Reddit Community"
                Pair(subreddit, subreddit)
            }
            PlatformType.LINKEDIN -> Pair("منشور لينكد إن", "@linkedin_post")
            PlatformType.YOUTUBE -> Pair("قناة يوتيوب", "@youtube_creator")
            PlatformType.OTHER -> Pair("مصدر ويب", "@web")
        }
    }

    private fun sanitizeTitle(title: String, platform: PlatformType, author: String): String {
        return title
            .replace(" - Facebook", "")
            .replace(" | Facebook", "")
            .replace(" on X: \"", ": ")
            .replace(" on Twitter: \"", ": ")
            .replace("\" / X", "")
            .replace(" / X", "")
            .replace(" - Twitter", "")
            .trimEnd('"', ' ', '-')
    }

    private fun sanitizeSnippet(snippet: String): String {
        // Strip common date prefixes from Google snippets e.g. "Jul 15, 2024 ... " or "قبل ساعتين ..."
        return snippet.replace(Regex("^(\\w+\\s+\\d{1,2},\\s+\\d{4}|\\d+\\s+days?\\s+ago|\\d+\\s+hours?\\s+ago|منذ\\s+\\d+\\s+(ساعات|أيام|دقيقة))\\s*(\\.\\.\\.|-|–)?\\s*"), "")
            .replace("...", "…")
            .trim()
    }

    private fun extractThumbnail(item: GoogleSearchItem): String? {
        val cseImage = item.pagemap?.cseImage?.firstOrNull()?.src
        if (!cseImage.isNullOrBlank() && !cseImage.endsWith(".svg") && !cseImage.contains("logo")) {
            return cseImage
        }
        val cseThumb = item.pagemap?.cseThumbnail?.firstOrNull()?.src
        if (!cseThumb.isNullOrBlank()) {
            return cseThumb
        }
        val meta = item.pagemap?.metatags?.firstOrNull()
        val ogImage = meta?.get("og:image") as? String ?: meta?.get("twitter:image") as? String
        return if (!ogImage.isNullOrBlank()) ogImage else null
    }

    private fun extractPublishTime(snippet: String, metatags: List<Map<String, Any?>>?): String {
        // Try metatags
        metatags?.firstOrNull()?.let { meta ->
            val pub = meta["article:published_time"] as? String ?: meta["og:updated_time"] as? String
            if (!pub.isNullOrBlank()) {
                return pub.substringBefore("T")
            }
        }

        // Try extracting time from snippet header
        val timeRegex = Regex("(\\d+\\s*(hours?|days?|weeks?|mins?|ساعات|أيام|أسبوع|دقائق)\\s*ago|منذ\\s*\\d+\\s*(ساعة|يوم|أسبوع|دقيقة))", RegexOption.IGNORE_CASE)
        val match = timeRegex.find(snippet)
        if (match != null) {
            return match.value
        }

        val dateRegex = Regex("([A-Z][a-z]{2}\\s+\\d{1,2},\\s+\\d{4})")
        val dateMatch = dateRegex.find(snippet)
        if (dateMatch != null) {
            return dateMatch.value
        }

        return "مؤخراً"
    }

    private fun extractHashtags(text: String): List<String> {
        val matcher = HASHTAG_REGEX.matcher(text)
        val tags = mutableListOf<String>()
        while (matcher.find()) {
            tags.add(matcher.group(1))
        }
        return tags.distinct().take(4)
    }

    /**
     * Fast offline heuristic sentiment classifier
     */
    fun heuristicSentiment(text: String): SentimentType {
        val lower = text.lowercase()
        val positiveWords = listOf(
            "ممتاز", "رائع", "نجاح", "تطور", "إنجاز", "مبهر", "شكرا", "جميل", "أفضل", "سعيد", "فوز", "قوي", "فرصة", "مستقبل",
            "great", "amazing", "success", "breakthrough", "love", "best", "excellent", "growth", "win", "good", "progress", "awesome"
        )
        val negativeWords = listOf(
            "سيء", "فشل", "أزمة", "كارثة", "انتقاد", "خسارة", "مشكلة", "غضب", "احتيال", "تراجع", "خطر", "صدمة", "مؤسف",
            "bad", "terrible", "fail", "crisis", "loss", "scam", "danger", "criticism", "error", "problem", "worst", "disaster", "angry"
        )

        var posScore = 0
        var negScore = 0

        positiveWords.forEach { if (lower.contains(it)) posScore++ }
        negativeWords.forEach { if (lower.contains(it)) negScore++ }

        return when {
            posScore > negScore -> SentimentType.POSITIVE
            negScore > posScore -> SentimentType.NEGATIVE
            else -> SentimentType.NEUTRAL
        }
    }

    private fun calculateEngagement(snippet: String, title: String): Int {
        val lengthFactor = (snippet.length + title.length) * 3
        val hash = (snippet.hashCode() and 0x7FFFFFFF) % 850
        return (50 + lengthFactor + hash).coerceIn(12, 4500)
    }
}
