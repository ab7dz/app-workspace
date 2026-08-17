package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.model.SocialPost

@Entity(tableName = "bookmarked_posts")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val title: String,
    val snippet: String,
    val postUrl: String,
    val cleanUrl: String,
    val authorName: String,
    val authorHandle: String,
    val thumbnailUrl: String?,
    val publishedTime: String?,
    val engagementLikes: Int,
    val sentiment: String,
    val sentimentReason: String?,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toSocialPost(): SocialPost {
        val plat = try { PlatformType.valueOf(platform) } catch (e: Exception) { PlatformType.OTHER }
        val sent = try { SentimentType.valueOf(sentiment) } catch (e: Exception) { SentimentType.NEUTRAL }
        return SocialPost(
            id = id,
            platform = plat,
            title = title,
            snippet = snippet,
            postUrl = postUrl,
            cleanUrl = cleanUrl,
            authorName = authorName,
            authorHandle = authorHandle,
            thumbnailUrl = thumbnailUrl,
            publishedTime = publishedTime,
            engagementLikes = engagementLikes,
            sentiment = sent,
            sentimentReason = sentimentReason,
            isBookmarked = true
        )
    }

    companion object {
        fun fromSocialPost(post: SocialPost): BookmarkEntity {
            return BookmarkEntity(
                id = post.id,
                platform = post.platform.name,
                title = post.title,
                snippet = post.snippet,
                postUrl = post.postUrl,
                cleanUrl = post.cleanUrl,
                authorName = post.authorName,
                authorHandle = post.authorHandle,
                thumbnailUrl = post.thumbnailUrl,
                publishedTime = post.publishedTime,
                engagementLikes = post.engagementLikes,
                sentiment = post.sentiment.name,
                sentimentReason = post.sentimentReason
            )
        }
    }
}

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val dorkQuery: String,
    val timestamp: Long = System.currentTimeMillis()
)
