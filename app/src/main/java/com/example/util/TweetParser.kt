package com.example.util

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class TweetMediaItem(
    val type: String, // "photo" | "video" | "gif"
    val url: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Double? = null
)

data class ParsedTweet(
    val tweetUrl: String,
    val tweetId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val mediaItems: List<TweetMediaItem> = emptyList(),
    val postedAt: String,
    val likesCount: Long = 0L,
    val repostsCount: Long = 0L,
    val quoteAuthorName: String? = null,
    val quoteAuthorHandle: String? = null,
    val quoteText: String? = null,
    val isContentFetched: Boolean = false
)

object TweetParser {
    private val URL_REGEX = Regex(
        """https?://(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)/([a-zA-Z0-9_]+)/status/([0-9]+)(?:\S*)?""",
        RegexOption.IGNORE_CASE
    )
    private val GENERIC_URL_REGEX = Regex("""https?://[\w\d:#@%/;$()~_?+\-=\\.&]+""", RegexOption.IGNORE_CASE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val mediaListType = Types.newParameterizedType(List::class.java, TweetMediaItem::class.java)
    private val mediaAdapter = moshi.adapter<List<TweetMediaItem>>(mediaListType)

    fun isTweetUrl(text: String): Boolean = URL_REGEX.containsMatchIn(text)

    fun extractHandleAndId(text: String): Pair<String, String>? {
        val match = URL_REGEX.find(text) ?: return null
        val handle = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
        val tweetId = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() } ?: return null
        return handle to tweetId
    }

    fun extractCleanUrl(text: String): String? {
        val match = URL_REGEX.find(text)
        if (match != null) {
            val handle = match.groupValues.getOrNull(1)
            val tweetId = match.groupValues.getOrNull(2)
            if (!handle.isNullOrBlank() && !tweetId.isNullOrBlank()) {
                return "https://x.com/$handle/status/$tweetId"
            }
        }
        return GENERIC_URL_REGEX.find(text)?.value
    }

    fun parseSharedText(rawText: String): ParsedTweet {
        val cleanUrl = extractCleanUrl(rawText) ?: rawText.trim()
        val match = URL_REGEX.find(rawText)

        var handle = "X_User"
        var tweetId = System.currentTimeMillis().toString()

        if (match != null) {
            handle = match.groupValues.getOrNull(1)?.ifBlank { "X_User" } ?: "X_User"
            tweetId = match.groupValues.getOrNull(2)?.ifBlank { tweetId } ?: tweetId
        }

        var snippet = rawText.replace(URL_REGEX, "").trim()
        if (snippet == rawText.trim()) {
            snippet = rawText.replace(GENERIC_URL_REGEX, "").trim()
        }
        snippet = snippet.trim { it == '"' || it == '\'' || it == '-' || it == ':' || it.isWhitespace() }

        val displayName = when (handle.lowercase(Locale.US)) {
            "elonmusk" -> "Elon Musk"
            "androiddev" -> "Android Developers"
            "x" -> "X"
            "openai" -> "OpenAI"
            "google" -> "Google"
            else -> handle.replace('_', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                    }
                }
                .ifBlank { handle }
        }

        val hasRealSnippet = snippet.isNotEmpty() && !snippet.startsWith("?") && !snippet.startsWith("&")
        val postContent = if (hasRealSnippet) snippet else cleanUrl

        return ParsedTweet(
            tweetUrl = cleanUrl,
            tweetId = tweetId,
            authorName = displayName,
            authorHandle = if (handle.startsWith("@")) handle else "@$handle",
            content = postContent,
            mediaItems = emptyList(),
            postedAt = formatTodayDate(),
            isContentFetched = hasRealSnippet
        )
    }

    fun serializeMediaItems(items: List<TweetMediaItem>): String? {
        if (items.isEmpty()) return null
        return try {
            mediaAdapter.toJson(items)
        } catch (_: Exception) {
            null
        }
    }

    fun deserializeMediaItems(json: String?): List<TweetMediaItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            mediaAdapter.fromJson(json).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun formatTodayDate(): String {
        return try {
            SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date())
        } catch (_: Exception) {
            ""
        }
    }
}
