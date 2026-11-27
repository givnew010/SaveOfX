package com.example.util

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Locale

@JsonClass(generateAdapter = true)
data class PersonWorkItem(
    val id: String,
    val type: String, // "link", "photo", "video", "other"
    val title: String,
    val url: String,
    val note: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

data class PersonWorksSummary(
    val total: Int,
    val links: Int,
    val photos: Int,
    val videos: Int,
    val others: Int
)

object PersonWorkCodec {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val workListType = Types.newParameterizedType(List::class.java, PersonWorkItem::class.java)
    private val workListAdapter = moshi.adapter<List<PersonWorkItem>>(workListType)

    fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""
        val lower = trimmed.lowercase(Locale.US)
        if (lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.startsWith("content://") ||
            lower.startsWith("file://") ||
            lower.startsWith("data:")
        ) {
            return trimmed
        }
        return "https://$trimmed"
    }

    fun normalizeWorkType(rawType: String?): String {
        return when (rawType?.trim()?.lowercase(Locale.US)) {
            "link", "photo", "video", "other" -> rawType.trim().lowercase(Locale.US)
            else -> "link"
        }
    }

    fun detectWorkTypeFromUrl(rawUrl: String): String {
        val lower = rawUrl.trim().lowercase(Locale.US)
        if (lower.isEmpty()) return "link"
        if (lower.startsWith("data:image/") ||
            Regex("\\.(jpg|jpeg|png|webp|gif|bmp|avif|svg)(\\?|#|$)").containsMatchIn(lower)
        ) {
            return "photo"
        }
        if (lower.startsWith("data:video/") ||
            Regex("\\.(mp4|webm|mov|m4v|mkv)(\\?|#|$)").containsMatchIn(lower) ||
            Regex("(youtube\\.com/watch|youtu\\.be/|youtube\\.com/shorts/|vimeo\\.com/|dailymotion\\.com/|tiktok\\.com/)")
                .containsMatchIn(lower)
        ) {
            return "video"
        }
        return "link"
    }

    fun defaultWorkTitle(type: String): String {
        return when (normalizeWorkType(type)) {
            "photo" -> "صورة"
            "video" -> "فيديو"
            "other" -> "مرفق"
            else -> "رابط عمل"
        }
    }

    fun getWorkTypeLabel(type: String): String {
        return when (normalizeWorkType(type)) {
            "photo" -> "صورة"
            "video" -> "فيديو"
            "other" -> "أخرى"
            else -> "رابط عمل"
        }
    }

    fun createWorkItem(
        type: String,
        title: String,
        url: String,
        note: String? = null,
        now: Long = System.currentTimeMillis()
    ): PersonWorkItem? {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isEmpty()) return null
        val normalizedType = normalizeWorkType(type)
        val resolvedTitle = title.trim().ifEmpty { defaultWorkTitle(normalizedType) }
        val resolvedNote = note?.trim()?.takeIf { it.isNotEmpty() }
        val suffix = (now % 100000L).toString()
        return PersonWorkItem(
            id = "work_${now}_$suffix",
            type = normalizedType,
            title = resolvedTitle,
            url = normalizedUrl,
            note = resolvedNote,
            addedAt = now
        )
    }

    fun serializeWorks(items: List<PersonWorkItem>): String {
        return runCatching { workListAdapter.toJson(items) }.getOrDefault("[]")
    }

    fun parseWorks(worksJson: String?): List<PersonWorkItem> {
        if (worksJson.isNullOrBlank()) return emptyList()
        return runCatching {
            workListAdapter.fromJson(worksJson).orEmpty()
                .filter { it.url.isNotBlank() }
                .map { item ->
                    item.copy(
                        type = normalizeWorkType(item.type),
                        title = item.title.trim().ifEmpty { defaultWorkTitle(item.type) },
                        url = item.url.trim()
                    )
                }
        }.getOrDefault(emptyList())
    }

    fun summarizeWorks(items: List<PersonWorkItem>): PersonWorksSummary {
        var links = 0
        var photos = 0
        var videos = 0
        var others = 0
        for (item in items) {
            when (normalizeWorkType(item.type)) {
                "link" -> links += 1
                "photo" -> photos += 1
                "video" -> videos += 1
                else -> others += 1
            }
        }
        return PersonWorksSummary(
            total = items.size,
            links = links,
            photos = photos,
            videos = videos,
            others = others
        )
    }

    fun buildDedupeKey(name: String, role: String): String {
        return "${name.trim().lowercase(Locale.US)}::${role.trim().lowercase(Locale.US)}"
    }
}
