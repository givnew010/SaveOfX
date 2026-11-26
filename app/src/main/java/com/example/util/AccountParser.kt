package com.example.util

import java.util.Locale

data class ParsedAccount(
    val profileUrl: String,
    val handle: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val verified: Boolean = false,
    val isProfileFetched: Boolean = false
)

object AccountParser {
    private val PROFILE_URL_REGEX = Regex(
        """https?://(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)/([a-zA-Z0-9_]{1,15})(?:[/?#]\S*)?$""",
        RegexOption.IGNORE_CASE
    )
    private val ANY_X_URL_REGEX = Regex(
        """https?://(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)/([a-zA-Z0-9_]{1,15})(?:[/?#]\S*)?""",
        RegexOption.IGNORE_CASE
    )
    private val STATUS_REGEX = Regex("""/status/[0-9]+""", RegexOption.IGNORE_CASE)
    private val HANDLE_REGEX = Regex("""^@?([a-zA-Z0-9_]{1,15})$""")

    private val RESERVED_PATHS = setOf(
        "home",
        "explore",
        "search",
        "notifications",
        "messages",
        "i",
        "intent",
        "settings",
        "compose",
        "hashtag",
        "share",
        "login",
        "signup"
    )

    fun inferDisplayNameFromHandle(username: String): String {
        return when (username.lowercase(Locale.US)) {
            "elonmusk" -> "Elon Musk"
            "androiddev" -> "Android Developers"
            "x" -> "X"
            "openai" -> "OpenAI"
            "google" -> "Google"
            "naval" -> "Naval"
            "techarabic" -> "تقنية بالعربي"
            else -> username.replace('_', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                    }
                }
                .ifBlank { username }
        }
    }

    fun extractUsername(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val handleMatch = HANDLE_REGEX.find(trimmed)
        if (handleMatch != null) {
            val candidate = handleMatch.groupValues.getOrNull(1).orEmpty()
            if (candidate.isNotEmpty() && candidate.lowercase(Locale.US) !in RESERVED_PATHS) {
                return candidate
            }
            return null
        }

        val urlMatch = ANY_X_URL_REGEX.find(trimmed)
        if (urlMatch != null) {
            val candidate = urlMatch.groupValues.getOrNull(1).orEmpty()
            if (candidate.isNotEmpty() && candidate.lowercase(Locale.US) !in RESERVED_PATHS) {
                return candidate
            }
        }

        return null
    }

    fun isValidAccountInput(input: String): Boolean = extractUsername(input) != null

    fun isProfileUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (STATUS_REGEX.containsMatchIn(trimmed)) return false
        val match = PROFILE_URL_REGEX.find(trimmed) ?: return false
        val candidate = match.groupValues.getOrNull(1).orEmpty()
        return candidate.isNotEmpty() && candidate.lowercase(Locale.US) !in RESERVED_PATHS
    }

    fun normalizeProfileUrl(username: String): String = "https://x.com/$username"

    fun parseAccountInput(rawInput: String): ParsedAccount? {
        val username = extractUsername(rawInput) ?: return null
        return ParsedAccount(
            profileUrl = normalizeProfileUrl(username),
            handle = "@$username",
            username = username,
            displayName = inferDisplayNameFromHandle(username),
            avatarUrl = null,
            bio = "",
            followersCount = 0L,
            followingCount = 0L,
            verified = false,
            isProfileFetched = false
        )
    }
}
