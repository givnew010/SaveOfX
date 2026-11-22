package com.example.util

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

@JsonClass(generateAdapter = true)
data class TweetMediaItem(
    val type: String, // "photo", "video", "gif"
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
    val postedAt: String = "",
    val likesCount: Int = 0,
    val repostsCount: Int = 0,
    val quoteAuthorName: String? = null,
    val quoteAuthorHandle: String? = null,
    val quoteText: String? = null,
    val isContentFetched: Boolean = false
)

object TweetParser {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val mediaListAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, TweetMediaItem::class.java)
        moshi.adapter<List<TweetMediaItem>>(type)
    }

    private val URL_REGEX = Pattern.compile(
        "https?://(?:www\\.|mobile\\.)?(?:x\\.com|twitter\\.com|fxtwitter\\.com|vxtwitter\\.com)/([a-zA-Z0-9_]+)/status/([0-9]+)(?:\\S*)?",
        Pattern.CASE_INSENSITIVE
    )

    private val GENERIC_URL_REGEX = Pattern.compile(
        "https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+",
        Pattern.CASE_INSENSITIVE
    )

    fun isTweetUrl(text: String): Boolean {
        return URL_REGEX.matcher(text).find()
    }

    fun extractHandleAndId(text: String): Pair<String, String>? {
        val matcher = URL_REGEX.matcher(text)
        if (matcher.find()) {
            val handle = matcher.group(1) ?: return null
            val tweetId = matcher.group(2) ?: return null
            return handle to tweetId
        }
        return null
    }

    fun extractCleanUrl(text: String): String? {
        val matcher = URL_REGEX.matcher(text)
        if (matcher.find()) {
            val handle = matcher.group(1)
            val tweetId = matcher.group(2)
            return "https://x.com/$handle/status/$tweetId"
        }
        val genericMatcher = GENERIC_URL_REGEX.matcher(text)
        if (genericMatcher.find()) {
            return genericMatcher.group()
        }
        return null
    }

    fun parseSharedText(rawText: String): ParsedTweet {
        val cleanUrl = extractCleanUrl(rawText) ?: rawText.trim()
        val matcher = URL_REGEX.matcher(rawText)

        var handle = "X_User"
        var tweetId = System.currentTimeMillis().toString()

        if (matcher.find()) {
            handle = matcher.group(1) ?: "X_User"
            tweetId = matcher.group(2) ?: tweetId
        }

        // Remove the ENTIRE matched tweet URL (including any ?s=20&... query parameters)
        var snippet = URL_REGEX.matcher(rawText).replaceAll("").trim()
        if (snippet == rawText.trim()) {
            snippet = GENERIC_URL_REGEX.matcher(rawText).replaceAll("").trim()
        }
        snippet = snippet.trim().trim('"', '\'', '-', ':').trim()

        val displayName = when (handle.lowercase(Locale.ROOT)) {
            "elonmusk" -> "Elon Musk"
            "androiddev" -> "Android Developers"
            "x" -> "X"
            "openai" -> "OpenAI"
            "google" -> "Google"
            else -> handle.replace("_", " ").split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { char -> char.uppercase() }
                }
                .ifBlank { handle }
        }

        val formattedDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())

        val hasRealSnippet = snippet.isNotBlank() &&
            !snippet.startsWith("?") &&
            !snippet.startsWith("&")

        val postContent = if (hasRealSnippet) snippet else cleanUrl

        return ParsedTweet(
            tweetUrl = cleanUrl,
            tweetId = tweetId,
            authorName = displayName,
            authorHandle = if (handle.startsWith("@")) handle else "@$handle",
            authorAvatarUrl = null,
            content = postContent,
            mediaUrl = null,
            mediaType = null,
            mediaItems = emptyList(),
            postedAt = formattedDate,
            isContentFetched = hasRealSnippet
        )
    }

    fun serializeMediaItems(items: List<TweetMediaItem>): String? {
        if (items.isEmpty()) return null
        return try {
            mediaListAdapter.toJson(items)
        } catch (e: Exception) {
            null
        }
    }

    fun deserializeMediaItems(json: String?): List<TweetMediaItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            mediaListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
