package com.example.util

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class AccountFetchResult {
    data class Success(val account: ParsedAccount) : AccountFetchResult()
    data class Offline(val fallbackAccount: ParsedAccount) : AccountFetchResult()
    data class Failed(val fallbackAccount: ParsedAccount?, val reason: String) : AccountFetchResult()
}

@JsonClass(generateAdapter = true)
internal data class FxUserVerification(
    val verified: Boolean? = null
)

@JsonClass(generateAdapter = true)
internal data class FxUserProfile(
    val id: String? = null,
    val name: String? = null,
    @Json(name = "screen_name") val screenName: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    val description: String? = null,
    val followers: Long? = null,
    val following: Long? = null,
    val verification: FxUserVerification? = null
)

@JsonClass(generateAdapter = true)
internal data class FxUserResponse(
    val code: Int? = null,
    val user: FxUserProfile? = null
)

object AccountFetcher {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val userAdapter = moshi.adapter(FxUserResponse::class.java)

    suspend fun fetchAccount(context: Context, rawInput: String): AccountFetchResult {
        val fallback = AccountParser.parseAccountInput(rawInput)
            ?: return AccountFetchResult.Failed(null, "اسم المستخدم أو رابط الحساب غير صالح")

        if (!TweetFetcher.isNetworkAvailable(context)) {
            return AccountFetchResult.Offline(fallback)
        }

        return withContext(Dispatchers.IO) {
            val encodedUsername = URLEncoder.encode(fallback.username, "UTF-8")
            fetchJson("https://api.fxtwitter.com/$encodedUsername")?.let { json ->
                parseFxUserJson(json, fallback.username)?.let { parsed ->
                    return@withContext AccountFetchResult.Success(parsed)
                }
            }

            AccountFetchResult.Failed(
                fallbackAccount = fallback,
                reason = "تعذر جلب بيانات الحساب حالياً"
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

    internal fun parseFxUserJson(rawJson: String, fallbackUsername: String): ParsedAccount? {
        val root = try {
            userAdapter.fromJson(rawJson)
        } catch (_: Exception) {
            return null
        } ?: return null

        val user = root.user ?: return null
        val username = user.screenName?.trim()?.takeIf { it.isNotEmpty() } ?: fallbackUsername
        val displayName = user.name?.trim()?.takeIf { it.isNotEmpty() } ?: username

        return ParsedAccount(
            profileUrl = AccountParser.normalizeProfileUrl(username),
            handle = "@$username",
            username = username,
            displayName = displayName,
            avatarUrl = user.avatarUrl?.trim()?.takeIf { it.isNotEmpty() },
            bio = user.description?.trim().orEmpty(),
            followersCount = user.followers ?: 0L,
            followingCount = user.following ?: 0L,
            verified = user.verification?.verified ?: false,
            isProfileFetched = true
        )
    }
}
