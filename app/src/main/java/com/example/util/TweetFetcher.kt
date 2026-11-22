package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import java.util.regex.Pattern

sealed class TweetFetchResult {
    data class Success(val tweet: ParsedTweet) : TweetFetchResult()
    data class Offline(val fallbackTweet: ParsedTweet) : TweetFetchResult()
    data class Failed(val fallbackTweet: ParsedTweet, val reason: String) : TweetFetchResult()
}

// --- FxTwitter API DTOs ---
@JsonClass(generateAdapter = true)
data class FxTweetResponse(
    val code: Int? = null,
    val tweet: FxTweetDto? = null
)

@JsonClass(generateAdapter = true)
data class FxTweetDto(
    val id: String? = null,
    val text: String? = null,
    @Json(name = "raw_text") val rawText: FxRawTextDto? = null,
    val author: FxAuthorDto? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "created_timestamp") val createdTimestamp: Long? = null,
    val likes: Int? = null,
    val retweets: Int? = null,
    val media: FxMediaContainerDto? = null,
    val quote: FxQuoteDto? = null
)

@JsonClass(generateAdapter = true)
data class FxRawTextDto(val text: String? = null)

@JsonClass(generateAdapter = true)
data class FxAuthorDto(
    val name: String? = null,
    @Json(name = "screen_name") val screenName: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class FxMediaContainerDto(
    val all: List<FxMediaItemDto>? = null,
    val photos: List<FxMediaItemDto>? = null,
    val videos: List<FxMediaItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class FxMediaItemDto(
    val type: String? = null,
    val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    val duration: Double? = null
)

@JsonClass(generateAdapter = true)
data class FxQuoteDto(
    val text: String? = null,
    val author: FxAuthorDto? = null
)

// --- VxTwitter API DTOs ---
@JsonClass(generateAdapter = true)
data class VxTweetResponse(
    val tweetID: String? = null,
    val text: String? = null,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_screen_name") val userScreenName: String? = null,
    @Json(name = "user_profile_image_url") val userProfileImageUrl: String? = null,
    val likes: Int? = null,
    val retweets: Int? = null,
    val date: String? = null,
    @Json(name = "date_epoch") val dateEpoch: Long? = null,
    @Json(name = "media_extended") val mediaExtended: List<VxMediaItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class VxMediaItemDto(
    val type: String? = null,
    val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "duration_millis") val durationMillis: Long? = null
)

// --- X oEmbed DTO ---
@JsonClass(generateAdapter = true)
data class XOEmbedResponse(
    @Json(name = "author_name") val authorName: String? = null,
    val html: String? = null
)

object TweetFetcher {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val fxAdapter by lazy { moshi.adapter(FxTweetResponse::class.java) }
    private val vxAdapter by lazy { moshi.adapter(VxTweetResponse::class.java) }
    private val oEmbedAdapter by lazy { moshi.adapter(XOEmbedResponse::class.java) }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
            } else {
                @Suppress("DEPRECATION")
                cm.activeNetworkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchTweet(context: Context, rawUrlOrText: String): TweetFetchResult {
        val fallback = TweetParser.parseSharedText(rawUrlOrText)
        val handleAndId = TweetParser.extractHandleAndId(rawUrlOrText)
            ?: return TweetFetchResult.Failed(fallback, "رابط غير صالح")

        if (!isNetworkAvailable(context)) {
            return TweetFetchResult.Offline(
                fallback.copy(
                    content = fallback.tweetUrl,
                    isContentFetched = false
                )
            )
        }

        val (handle, tweetId) = handleAndId
        return withContext(Dispatchers.IO) {
            val fxResult = fetchFromFxTwitter(handle, tweetId, fallback.tweetUrl)
            if (fxResult != null) {
                return@withContext TweetFetchResult.Success(fxResult)
            }

            val vxResult = fetchFromVxTwitter(handle, tweetId, fallback.tweetUrl)
            if (vxResult != null) {
                return@withContext TweetFetchResult.Success(vxResult)
            }

            val oEmbedResult = fetchFromOEmbed(fallback)
            if (oEmbedResult != null) {
                return@withContext TweetFetchResult.Success(oEmbedResult)
            }

            TweetFetchResult.Failed(
                fallback.copy(
                    content = fallback.tweetUrl,
                    isContentFetched = false
                ),
                "تعذر جلب تفاصيل المنشور"
            )
        }
    }

    private fun executeGet(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "XFoldersAndroid/1.0")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchFromFxTwitter(handle: String, tweetId: String, cleanUrl: String): ParsedTweet? {
        val body = executeGet("https://api.fxtwitter.com/$handle/status/$tweetId") ?: return null
        return parseFxTwitterJson(body, cleanUrl)
    }

    private fun fetchFromVxTwitter(handle: String, tweetId: String, cleanUrl: String): ParsedTweet? {
        val body = executeGet("https://api.vxtwitter.com/$handle/status/$tweetId") ?: return null
        return parseVxTwitterJson(body, cleanUrl)
    }

    private fun fetchFromOEmbed(fallback: ParsedTweet): ParsedTweet? {
        val encodedUrl = URLEncoder.encode(fallback.tweetUrl, "UTF-8")
        val body = executeGet("https://publish.twitter.com/oembed?url=$encodedUrl&omit_script=true")
            ?: return null
        return parseOEmbedJson(body, fallback)
    }

    fun parseFxTwitterJson(jsonStr: String, cleanUrl: String): ParsedTweet? {
        return try {
            val root = fxAdapter.fromJson(jsonStr) ?: return null
            val tweetObj = root.tweet ?: return null

            val text = tweetObj.rawText?.text?.takeIf { it.isNotBlank() }
                ?: tweetObj.text.orEmpty()

            val authorName = tweetObj.author?.name?.takeIf { it.isNotBlank() } ?: "X User"
            val screenName = tweetObj.author?.screenName?.takeIf { it.isNotBlank() } ?: "x"
            val avatarUrl = tweetObj.author?.avatarUrl?.takeIf { it.isNotBlank() }

            val createdTs = tweetObj.createdTimestamp ?: 0L
            val postedAt = if (createdTs > 0L) {
                SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(createdTs * 1000L))
            } else {
                tweetObj.createdAt.orEmpty().take(16)
            }

            val rawMediaList = when {
                !tweetObj.media?.all.isNullOrEmpty() -> tweetObj.media?.all.orEmpty()
                else -> tweetObj.media?.photos.orEmpty() + tweetObj.media?.videos.orEmpty()
            }

            val mediaItems = rawMediaList.mapNotNull { m ->
                val url = m.url?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val rawType = (m.type ?: "photo").lowercase(Locale.ROOT)
                val normalizedType = when {
                    rawType.contains("video") -> "video"
                    rawType.contains("gif") -> "gif"
                    else -> "photo"
                }
                TweetMediaItem(
                    type = normalizedType,
                    url = url,
                    thumbnailUrl = m.thumbnailUrl?.takeIf { it.isNotBlank() },
                    durationSeconds = m.duration
                )
            }

            val mediaTypeSummary = when {
                mediaItems.isEmpty() -> null
                mediaItems.any { it.type == "video" } -> "video"
                mediaItems.any { it.type == "gif" } -> "gif"
                else -> "photo"
            }

            val primaryMediaUrl = mediaItems.firstOrNull()?.thumbnailUrl ?: mediaItems.firstOrNull()?.url

            ParsedTweet(
                tweetUrl = cleanUrl,
                tweetId = tweetObj.id?.takeIf { it.isNotBlank() } ?: cleanUrl.substringAfterLast("/"),
                authorName = authorName,
                authorHandle = "@$screenName",
                authorAvatarUrl = avatarUrl,
                content = text.ifBlank { if (mediaItems.isNotEmpty()) "" else cleanUrl },
                mediaUrl = primaryMediaUrl,
                mediaType = mediaTypeSummary,
                mediaItems = mediaItems,
                postedAt = postedAt,
                likesCount = tweetObj.likes ?: 0,
                repostsCount = tweetObj.retweets ?: 0,
                quoteAuthorName = tweetObj.quote?.author?.name?.takeIf { it.isNotBlank() },
                quoteAuthorHandle = tweetObj.quote?.author?.screenName?.takeIf { it.isNotBlank() }?.let { "@$it" },
                quoteText = tweetObj.quote?.text?.takeIf { it.isNotBlank() },
                isContentFetched = true
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseVxTwitterJson(jsonStr: String, cleanUrl: String): ParsedTweet? {
        return try {
            val root = vxAdapter.fromJson(jsonStr) ?: return null
            if (root.text == null && root.userName == null) return null

            val text = root.text.orEmpty()
            val authorName = root.userName?.takeIf { it.isNotBlank() } ?: "X User"
            val screenName = root.userScreenName?.takeIf { it.isNotBlank() } ?: "x"
            val avatarUrl = root.userProfileImageUrl?.takeIf { it.isNotBlank() }
            val dateEpoch = root.dateEpoch ?: 0L
            val postedAt = if (dateEpoch > 0L) {
                SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(dateEpoch * 1000L))
            } else {
                root.date.orEmpty().take(16)
            }

            val mediaItems = root.mediaExtended.orEmpty().mapNotNull { m ->
                val url = m.url?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val rawType = (m.type ?: "image").lowercase(Locale.ROOT)
                val normalizedType = when (rawType) {
                    "video" -> "video"
                    "gif" -> "gif"
                    else -> "photo"
                }
                TweetMediaItem(
                    type = normalizedType,
                    url = url,
                    thumbnailUrl = m.thumbnailUrl?.takeIf { it.isNotBlank() },
                    durationSeconds = m.durationMillis?.let { it / 1000.0 }
                )
            }

            val mediaTypeSummary = when {
                mediaItems.isEmpty() -> null
                mediaItems.any { it.type == "video" } -> "video"
                mediaItems.any { it.type == "gif" } -> "gif"
                else -> "photo"
            }

            ParsedTweet(
                tweetUrl = cleanUrl,
                tweetId = root.tweetID?.takeIf { it.isNotBlank() } ?: cleanUrl.substringAfterLast("/"),
                authorName = authorName,
                authorHandle = "@$screenName",
                authorAvatarUrl = avatarUrl,
                content = text.ifBlank { if (mediaItems.isNotEmpty()) "" else cleanUrl },
                mediaUrl = mediaItems.firstOrNull()?.thumbnailUrl ?: mediaItems.firstOrNull()?.url,
                mediaType = mediaTypeSummary,
                mediaItems = mediaItems,
                postedAt = postedAt,
                likesCount = root.likes ?: 0,
                repostsCount = root.retweets ?: 0,
                isContentFetched = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseOEmbedJson(jsonStr: String, fallback: ParsedTweet): ParsedTweet? {
        return try {
            val root = oEmbedAdapter.fromJson(jsonStr) ?: return null
            val authorName = root.authorName?.takeIf { it.isNotBlank() } ?: fallback.authorName
            val html = root.html.orEmpty()

            val pPattern = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL)
            val matcher = pPattern.matcher(html)
            val extractedText = if (matcher.find()) {
                val inner = matcher.group(1) ?: ""
                inner.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                    .replace(Regex("<[^>]*>"), "")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .trim()
            } else {
                ""
            }

            if (extractedText.isBlank()) return null

            fallback.copy(
                authorName = authorName,
                content = extractedText,
                isContentFetched = true
            )
        } catch (e: Exception) {
            null
        }
    }
}
