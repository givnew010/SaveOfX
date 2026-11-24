package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class TweetFetchResult {
    data class Success(val tweet: ParsedTweet) : TweetFetchResult()
    data class Offline(val fallbackTweet: ParsedTweet) : TweetFetchResult()
    data class Failed(val fallbackTweet: ParsedTweet, val reason: String) : TweetFetchResult()
}

@JsonClass(generateAdapter = true)
internal data class FxMediaItem(
    val type: String? = null,
    val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    val duration: Double? = null
)

@JsonClass(generateAdapter = true)
internal data class FxRawText(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
internal data class FxAuthor(
    val name: String? = null,
    @Json(name = "screen_name") val screenName: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
internal data class FxMediaContainer(
    val all: List<FxMediaItem>? = null,
    val photos: List<FxMediaItem>? = null,
    val videos: List<FxMediaItem>? = null
)

@JsonClass(generateAdapter = true)
internal data class FxQuote(
    val text: String? = null,
    val author: FxAuthor? = null
)

@JsonClass(generateAdapter = true)
internal data class FxTweetBody(
    val id: String? = null,
    val text: String? = null,
    @Json(name = "raw_text") val rawText: FxRawText? = null,
    val author: FxAuthor? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "created_timestamp") val createdTimestamp: Long? = null,
    val likes: Long? = null,
    val retweets: Long? = null,
    val media: FxMediaContainer? = null,
    val quote: FxQuote? = null
)

@JsonClass(generateAdapter = true)
internal data class FxTweetResponse(
    val code: Int? = null,
    val tweet: FxTweetBody? = null
)

@JsonClass(generateAdapter = true)
internal data class VxMediaItem(
    val type: String? = null,
    val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "duration_millis") val durationMillis: Long? = null
)

@JsonClass(generateAdapter = true)
internal data class VxTweetResponse(
    val tweetID: String? = null,
    val text: String? = null,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_screen_name") val userScreenName: String? = null,
    @Json(name = "user_profile_image_url") val userProfileImageUrl: String? = null,
    val likes: Long? = null,
    val retweets: Long? = null,
    val date: String? = null,
    @Json(name = "date_epoch") val dateEpoch: Long? = null,
    @Json(name = "media_extended") val mediaExtended: List<VxMediaItem>? = null
)

@JsonClass(generateAdapter = true)
internal data class XOEmbedResponse(
    @Json(name = "author_name") val authorName: String? = null,
    val html: String? = null
)

object TweetFetcher {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val fxAdapter = moshi.adapter(FxTweetResponse::class.java)
    private val vxAdapter = moshi.adapter(VxTweetResponse::class.java)
    private val oEmbedAdapter = moshi.adapter(XOEmbedResponse::class.java)

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun fetchTweet(context: Context, rawUrlOrText: String): TweetFetchResult {
        val fallback = TweetParser.parseSharedText(rawUrlOrText)
        val handleAndId = TweetParser.extractHandleAndId(rawUrlOrText)
            ?: return TweetFetchResult.Failed(fallback, "رابط غير صالح")

        if (!isNetworkAvailable(context)) {
            return TweetFetchResult.Offline(
                fallback.copy(content = fallback.tweetUrl, isContentFetched = false)
            )
        }

        val (handle, tweetId) = handleAndId
        val cleanUrl = fallback.tweetUrl

        return withContext(Dispatchers.IO) {
            fetchJson("https://api.fxtwitter.com/$handle/status/$tweetId")?.let { json ->
                parseFxTwitterJson(json, cleanUrl)?.let { parsed ->
                    return@withContext TweetFetchResult.Success(parsed)
                }
            }

            fetchJson("https://api.vxtwitter.com/$handle/status/$tweetId")?.let { json ->
                parseVxTwitterJson(json, cleanUrl)?.let { parsed ->
                    return@withContext TweetFetchResult.Success(parsed)
                }
            }

            val encoded = URLEncoder.encode(cleanUrl, "UTF-8")
            fetchJson("https://publish.twitter.com/oembed?url=$encoded&omit_script=true")?.let { json ->
                parseOEmbedJson(json, handle, tweetId, cleanUrl)?.let { parsed ->
                    return@withContext TweetFetchResult.Success(parsed)
                }
            }

            TweetFetchResult.Failed(
                fallback.copy(content = fallback.tweetUrl, isContentFetched = false),
                "تعذر جلب تفاصيل المنشور"
            )
        }
    }

    private fun fetchJson(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "XFoldersAndroid/1.0")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                response.body?.string()
            }
        } catch (_: Exception) {
            null
        }
    }

    internal fun parseFxTwitterJson(rawJson: String, cleanUrl: String): ParsedTweet? {
        val root = try {
            fxAdapter.fromJson(rawJson)
        } catch (_: Exception) {
            return null
        } ?: return null

        val tweetObj = root.tweet ?: return null
        val text = tweetObj.rawText?.text?.takeIf { it.isNotBlank() } ?: tweetObj.text.orEmpty()
        val authorName = tweetObj.author?.name?.trim()?.takeIf { it.isNotEmpty() } ?: "X User"
        val screenName = tweetObj.author?.screenName?.trim()?.takeIf { it.isNotEmpty() } ?: "x"
        val avatarUrl = tweetObj.author?.avatarUrl?.trim()?.takeIf { it.isNotEmpty() }
        val createdTs = tweetObj.createdTimestamp ?: 0L
        val postedAt = if (createdTs > 0L) {
            formatEpochDate(createdTs)
        } else {
            tweetObj.createdAt.orEmpty().take(16)
        }

        val rawMedia = tweetObj.media?.all?.takeIf { it.isNotEmpty() }
            ?: (tweetObj.media?.photos.orEmpty() + tweetObj.media?.videos.orEmpty())

        val mediaItems = rawMedia.mapNotNull { item ->
            val mediaUrl = item.url?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val rawType = item.type.orEmpty().lowercase(Locale.US)
            val normalizedType = when {
                rawType.contains("video") -> "video"
                rawType.contains("gif") -> "gif"
                else -> "photo"
            }
            TweetMediaItem(
                type = normalizedType,
                url = mediaUrl,
                thumbnailUrl = item.thumbnailUrl?.trim()?.takeIf { it.isNotEmpty() },
                durationSeconds = item.duration
            )
        }

        val mediaTypeSummary = when {
            mediaItems.any { it.type == "video" } -> "video"
            mediaItems.any { it.type == "gif" } -> "gif"
            mediaItems.isNotEmpty() -> "photo"
            else -> null
        }

        val primaryMediaUrl = mediaItems.firstOrNull()?.let { it.thumbnailUrl ?: it.url }

        return ParsedTweet(
            tweetUrl = cleanUrl,
            tweetId = tweetObj.id?.trim()?.takeIf { it.isNotEmpty() } ?: cleanUrl.substringAfterLast('/'),
            authorName = authorName,
            authorHandle = "@$screenName",
            authorAvatarUrl = avatarUrl,
            content = when {
                text.isNotBlank() -> text
                mediaItems.isNotEmpty() -> ""
                else -> cleanUrl
            },
            mediaUrl = primaryMediaUrl,
            mediaType = mediaTypeSummary,
            mediaItems = mediaItems,
            postedAt = postedAt,
            likesCount = tweetObj.likes ?: 0L,
            repostsCount = tweetObj.retweets ?: 0L,
            quoteAuthorName = tweetObj.quote?.author?.name?.trim()?.takeIf { it.isNotEmpty() },
            quoteAuthorHandle = tweetObj.quote?.author?.screenName?.trim()?.takeIf { it.isNotEmpty() }?.let { "@$it" },
            quoteText = tweetObj.quote?.text?.trim()?.takeIf { it.isNotEmpty() },
            isContentFetched = true
        )
    }

    internal fun parseVxTwitterJson(rawJson: String, cleanUrl: String): ParsedTweet? {
        val root = try {
            vxAdapter.fromJson(rawJson)
        } catch (_: Exception) {
            return null
        } ?: return null

        if (root.text == null && root.userName == null) return null

        val text = root.text.orEmpty()
        val authorName = root.userName?.trim()?.takeIf { it.isNotEmpty() } ?: "X User"
        val screenName = root.userScreenName?.trim()?.takeIf { it.isNotEmpty() } ?: "x"
        val avatarUrl = root.userProfileImageUrl?.trim()?.takeIf { it.isNotEmpty() }
        val dateEpoch = root.dateEpoch ?: 0L
        val postedAt = if (dateEpoch > 0L) formatEpochDate(dateEpoch) else root.date.orEmpty().take(16)

        val mediaItems = root.mediaExtended.orEmpty().mapNotNull { item ->
            val mediaUrl = item.url?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val rawType = item.type.orEmpty().lowercase(Locale.US)
            val normalizedType = when (rawType) {
                "video" -> "video"
                "gif" -> "gif"
                else -> "photo"
            }
            TweetMediaItem(
                type = normalizedType,
                url = mediaUrl,
                thumbnailUrl = item.thumbnailUrl?.trim()?.takeIf { it.isNotEmpty() },
                durationSeconds = item.durationMillis?.let { it / 1000.0 }
            )
        }

        val mediaTypeSummary = when {
            mediaItems.any { it.type == "video" } -> "video"
            mediaItems.any { it.type == "gif" } -> "gif"
            mediaItems.isNotEmpty() -> "photo"
            else -> null
        }

        return ParsedTweet(
            tweetUrl = cleanUrl,
            tweetId = root.tweetID?.trim()?.takeIf { it.isNotEmpty() } ?: cleanUrl.substringAfterLast('/'),
            authorName = authorName,
            authorHandle = "@$screenName",
            authorAvatarUrl = avatarUrl,
            content = when {
                text.isNotBlank() -> text
                mediaItems.isNotEmpty() -> ""
                else -> cleanUrl
            },
            mediaUrl = mediaItems.firstOrNull()?.let { it.thumbnailUrl ?: it.url },
            mediaType = mediaTypeSummary,
            mediaItems = mediaItems,
            postedAt = postedAt,
            likesCount = root.likes ?: 0L,
            repostsCount = root.retweets ?: 0L,
            isContentFetched = true
        )
    }

    internal fun parseOEmbedJson(
        rawJson: String,
        handle: String,
        tweetId: String,
        cleanUrl: String
    ): ParsedTweet? {
        val root = try {
            oEmbedAdapter.fromJson(rawJson)
        } catch (_: Exception) {
            return null
        } ?: return null

        val html = root.html ?: return null
        val pMatch = Regex("""<p[^>]*>([\s\S]*?)</p>""", RegexOption.IGNORE_CASE).find(html)
            ?: return null
        val extractedText = pMatch.groupValues[1]
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""<[^>]*>"""), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .trim()

        if (extractedText.isEmpty()) return null

        return ParsedTweet(
            tweetUrl = cleanUrl,
            tweetId = tweetId,
            authorName = root.authorName?.trim()?.takeIf { it.isNotEmpty() } ?: handle,
            authorHandle = "@$handle",
            content = extractedText,
            mediaItems = emptyList(),
            postedAt = formatEpochDate(System.currentTimeMillis() / 1000L),
            isContentFetched = true
        )
    }

    private fun formatEpochDate(epochSeconds: Long): String {
        return try {
            SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date(epochSeconds * 1000L))
        } catch (_: Exception) {
            ""
        }
    }
}
