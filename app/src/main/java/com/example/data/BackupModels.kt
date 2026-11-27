package com.example.data

import com.example.util.PersonWorkItem
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupFolderRecord(
    val id: Long? = null,
    val name: String? = null,
    val iconEmoji: String? = null,
    val colorHex: String? = null,
    val description: String? = null,
    val createdAt: Long? = null,
    val isDefault: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class BackupBookmarkRecord(
    val id: Long? = null,
    val folderId: Long? = null,
    val tweetUrl: String? = null,
    val tweetId: String? = null,
    val authorName: String? = null,
    val authorHandle: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String? = null,
    val mediaUrl: String? = null,
    val postedAt: String? = null,
    val savedAt: Long? = null,
    val userNote: String? = null,
    val tags: String? = null,
    val isFavorite: Boolean? = null,
    val likesCount: Long? = null,
    val repostsCount: Long? = null,
    val isContentFetched: Boolean? = null,
    val mediaType: String? = null,
    val mediaJson: String? = null,
    val quoteAuthorName: String? = null,
    val quoteAuthorHandle: String? = null,
    val quoteText: String? = null
)

@JsonClass(generateAdapter = true)
data class BackupAccountRecord(
    val id: Long? = null,
    val folderId: Long? = null,
    val profileUrl: String? = null,
    val handle: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val followersCount: Long? = null,
    val followingCount: Long? = null,
    val verified: Boolean? = null,
    val userNote: String? = null,
    val tags: String? = null,
    val isFavorite: Boolean? = null,
    val savedAt: Long? = null,
    val isProfileFetched: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class BackupPersonRecord(
    val id: Long? = null,
    val folderId: Long? = null,
    val name: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val userNote: String? = null,
    val tags: String? = null,
    val worksJson: String? = null,
    val works: List<PersonWorkItem>? = null,
    val isFavorite: Boolean? = null,
    val savedAt: Long? = null
)

@JsonClass(generateAdapter = true)
data class RawBackupEnvelope(
    val app: String? = null,
    val version: Int? = null,
    val exportedAt: String? = null,
    val folders: List<BackupFolderRecord>? = null,
    val bookmarks: List<BackupBookmarkRecord>? = null,
    val accounts: List<BackupAccountRecord>? = null,
    val people: List<BackupPersonRecord>? = null
)

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val app: String = "X Folders",
    val version: Int = 3,
    val exportedAt: String,
    val folders: List<FolderEntity>,
    val bookmarks: List<TweetBookmarkEntity>,
    val accounts: List<AccountBookmarkEntity> = emptyList(),
    val people: List<PersonEntity> = emptyList()
)

enum class RestoreMode {
    MERGE,
    REPLACE
}

data class RestoreSummary(
    val foldersAdded: Int,
    val bookmarksAdded: Int,
    val accountsAdded: Int = 0,
    val peopleAdded: Int = 0
)

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode {
            return entries.firstOrNull { it.key == key } ?: SYSTEM
        }
    }
}
