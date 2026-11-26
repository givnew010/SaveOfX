package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class RestorePlan(
    val foldersToSave: List<FolderEntity>,
    val bookmarksToSave: List<TweetBookmarkEntity>,
    val accountsToSave: List<AccountBookmarkEntity>,
    val foldersAddedCount: Int,
    val bookmarksAddedCount: Int,
    val accountsAddedCount: Int,
    val isReplace: Boolean
)

object BackupManager {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val rawAdapter = moshi.adapter(RawBackupEnvelope::class.java)
    private val payloadAdapter = moshi.adapter(BackupPayload::class.java).indent("  ")

    fun nowIsoTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }

    fun buildPayload(
        folders: List<FolderEntity>,
        bookmarks: List<TweetBookmarkEntity>,
        accounts: List<AccountBookmarkEntity> = emptyList(),
        exportedAt: String = nowIsoTimestamp()
    ): BackupPayload {
        return BackupPayload(
            app = "X Folders",
            version = 2,
            exportedAt = exportedAt,
            folders = folders,
            bookmarks = bookmarks,
            accounts = accounts
        )
    }

    fun serializePayload(payload: BackupPayload): String {
        return payloadAdapter.toJson(payload)
    }

    fun parseBackupJson(rawJson: String): BackupPayload? {
        if (rawJson.isBlank()) return null
        val raw = try {
            rawAdapter.fromJson(rawJson)
        } catch (_: Exception) {
            return null
        } ?: return null

        val rawFolders = raw.folders ?: return null
        val rawBookmarks = raw.bookmarks ?: return null
        val rawAccounts = raw.accounts.orEmpty()
        val now = System.currentTimeMillis()

        val validFolders = mutableListOf<FolderEntity>()
        for (item in rawFolders) {
            val name = item.name?.trim().orEmpty()
            if (name.isEmpty()) continue
            val candidateId = item.id?.takeIf { it > 0L } ?: (validFolders.size + 1L)
            validFolders.add(
                FolderEntity(
                    id = candidateId,
                    name = name,
                    iconEmoji = item.iconEmoji?.trim()?.takeIf { it.isNotEmpty() } ?: "📁",
                    colorHex = item.colorHex?.trim()?.takeIf { it.isNotEmpty() } ?: "#1D9BF0",
                    description = item.description.orEmpty(),
                    createdAt = item.createdAt ?: now,
                    isDefault = item.isDefault ?: false
                )
            )
        }

        val validBookmarks = mutableListOf<TweetBookmarkEntity>()
        for (item in rawBookmarks) {
            val tweetUrl = item.tweetUrl?.trim().orEmpty()
            if (tweetUrl.isEmpty()) continue
            val normalizedMediaType = when (item.mediaType) {
                "photo", "video", "gif" -> item.mediaType
                else -> null
            }
            val candidateId = item.id?.takeIf { it > 0L } ?: (validBookmarks.size + 1L)
            validBookmarks.add(
                TweetBookmarkEntity(
                    id = candidateId,
                    folderId = item.folderId?.takeIf { it > 0L } ?: 1L,
                    tweetUrl = tweetUrl,
                    tweetId = item.tweetId.orEmpty(),
                    authorName = item.authorName?.trim()?.takeIf { it.isNotEmpty() } ?: "X User",
                    authorHandle = item.authorHandle?.trim()?.takeIf { it.isNotEmpty() } ?: "@x",
                    authorAvatarUrl = item.authorAvatarUrl?.takeIf { it.isNotBlank() },
                    content = item.content ?: tweetUrl,
                    mediaUrl = item.mediaUrl?.takeIf { it.isNotBlank() },
                    postedAt = item.postedAt.orEmpty(),
                    savedAt = item.savedAt ?: now,
                    userNote = item.userNote?.trim()?.takeIf { it.isNotEmpty() },
                    tags = item.tags.orEmpty(),
                    isFavorite = item.isFavorite ?: false,
                    likesCount = item.likesCount ?: 0L,
                    repostsCount = item.repostsCount ?: 0L,
                    isContentFetched = item.isContentFetched ?: false,
                    mediaType = normalizedMediaType,
                    mediaJson = item.mediaJson?.takeIf { it.isNotBlank() },
                    quoteAuthorName = item.quoteAuthorName?.takeIf { it.isNotBlank() },
                    quoteAuthorHandle = item.quoteAuthorHandle?.takeIf { it.isNotBlank() },
                    quoteText = item.quoteText?.takeIf { it.isNotBlank() }
                )
            )
        }

        val validAccounts = mutableListOf<AccountBookmarkEntity>()
        for (item in rawAccounts) {
            val profileUrl = item.profileUrl?.trim().orEmpty()
            val rawHandle = item.handle?.trim().orEmpty()
            if (profileUrl.isEmpty() || rawHandle.isEmpty()) continue
            val normalizedHandle = if (rawHandle.startsWith("@")) rawHandle else "@$rawHandle"
            val candidateId = item.id?.takeIf { it > 0L } ?: (validAccounts.size + 1L)
            validAccounts.add(
                AccountBookmarkEntity(
                    id = candidateId,
                    folderId = item.folderId?.takeIf { it > 0L } ?: 1L,
                    profileUrl = profileUrl,
                    handle = normalizedHandle,
                    displayName = item.displayName?.trim()?.takeIf { it.isNotEmpty() }
                        ?: normalizedHandle.removePrefix("@"),
                    avatarUrl = item.avatarUrl?.takeIf { it.isNotBlank() },
                    bio = item.bio.orEmpty(),
                    followersCount = item.followersCount ?: 0L,
                    followingCount = item.followingCount ?: 0L,
                    verified = item.verified ?: false,
                    userNote = item.userNote?.trim()?.takeIf { it.isNotEmpty() },
                    tags = item.tags.orEmpty(),
                    isFavorite = item.isFavorite ?: false,
                    savedAt = item.savedAt ?: now,
                    isProfileFetched = item.isProfileFetched ?: false
                )
            )
        }

        if (validFolders.isEmpty() && validBookmarks.isEmpty() && validAccounts.isEmpty()) return null

        return BackupPayload(
            app = raw.app?.takeIf { it.isNotBlank() } ?: "X Folders",
            version = raw.version ?: 2,
            exportedAt = raw.exportedAt?.takeIf { it.isNotBlank() } ?: nowIsoTimestamp(),
            folders = validFolders,
            bookmarks = validBookmarks,
            accounts = validAccounts
        )
    }

    fun planRestore(
        payload: BackupPayload,
        mode: RestoreMode,
        existingFolders: List<FolderEntity>,
        existingBookmarks: List<TweetBookmarkEntity>,
        existingAccounts: List<AccountBookmarkEntity> = emptyList()
    ): RestorePlan {
        if (mode == RestoreMode.REPLACE) {
            var maxFolderId = payload.folders.maxOfOrNull { it.id } ?: 0L
            val seenFolderIds = mutableSetOf<Long>()
            val folderIdMap = mutableMapOf<Long, Long>()

            val normalizedFolders = payload.folders.mapIndexed { index, folder ->
                val baseId = if (folder.id > 0L) folder.id else (index + 1L)
                val finalId = if (seenFolderIds.add(baseId)) {
                    baseId
                } else {
                    ++maxFolderId
                }
                folderIdMap[folder.id] = finalId
                folder.copy(id = finalId)
            }.toMutableList()

            if (normalizedFolders.isEmpty()) {
                normalizedFolders.add(
                    FolderEntity(
                        id = 1L,
                        name = "العلامات المرجعية العامة",
                        iconEmoji = "📌",
                        colorHex = "#1D9BF0",
                        description = "المجموعة الافتراضية لجميع العناصر غير المصنفة",
                        createdAt = System.currentTimeMillis(),
                        isDefault = true
                    )
                )
            }

            if (normalizedFolders.none { it.isDefault }) {
                normalizedFolders[0] = normalizedFolders[0].copy(isDefault = true)
            }

            val defaultFolderId = normalizedFolders.firstOrNull { it.isDefault }?.id
                ?: normalizedFolders.first().id
            val validFolderIds = normalizedFolders.map { it.id }.toSet()

            val normalizedBookmarks = payload.bookmarks.mapIndexed { index, bookmark ->
                val mappedFolderId = folderIdMap[bookmark.folderId] ?: bookmark.folderId
                val finalFolderId = if (mappedFolderId in validFolderIds) mappedFolderId else defaultFolderId
                bookmark.copy(
                    id = index + 1L,
                    folderId = finalFolderId
                )
            }

            val normalizedAccounts = payload.accounts.mapIndexed { index, account ->
                val mappedFolderId = folderIdMap[account.folderId] ?: account.folderId
                val finalFolderId = if (mappedFolderId in validFolderIds) mappedFolderId else defaultFolderId
                account.copy(
                    id = index + 1L,
                    folderId = finalFolderId
                )
            }

            return RestorePlan(
                foldersToSave = normalizedFolders,
                bookmarksToSave = normalizedBookmarks,
                accountsToSave = normalizedAccounts,
                foldersAddedCount = normalizedFolders.size,
                bookmarksAddedCount = normalizedBookmarks.size,
                accountsAddedCount = normalizedAccounts.size,
                isReplace = true
            )
        }

        // MERGE mode
        val defaultExisting = existingFolders.firstOrNull { it.isDefault } ?: existingFolders.firstOrNull()
        val defaultFolderId = defaultExisting?.id ?: 1L
        val folderIdMap = mutableMapOf<Long, Long>()
        var nextFolderId = (existingFolders.maxOfOrNull { it.id } ?: 0L) + 1L
        val newFoldersToInsert = mutableListOf<FolderEntity>()

        for (incoming in payload.folders) {
            val matchByDefault = if (incoming.isDefault) defaultExisting else null
            val matchByName = existingFolders.firstOrNull {
                it.name.trim().equals(incoming.name.trim(), ignoreCase = true)
            }
            val matched = matchByDefault ?: matchByName
            if (matched != null) {
                folderIdMap[incoming.id] = matched.id
            } else {
                val assignedId = nextFolderId++
                folderIdMap[incoming.id] = assignedId
                newFoldersToInsert.add(
                    incoming.copy(
                        id = assignedId,
                        isDefault = false
                    )
                )
            }
        }

        val existingUrlSet = existingBookmarks
            .map { it.tweetUrl.trim().lowercase(Locale.US) }
            .toMutableSet()
        var nextBookmarkId = (existingBookmarks.maxOfOrNull { it.id } ?: 0L) + 1L
        val newBookmarksToInsert = mutableListOf<TweetBookmarkEntity>()

        for (incoming in payload.bookmarks) {
            val normalizedUrl = incoming.tweetUrl.trim().lowercase(Locale.US)
            if (normalizedUrl in existingUrlSet) continue
            existingUrlSet.add(normalizedUrl)
            val targetFolderId = folderIdMap[incoming.folderId] ?: defaultFolderId
            newBookmarksToInsert.add(
                incoming.copy(
                    id = nextBookmarkId++,
                    folderId = targetFolderId
                )
            )
        }

        val existingAccountUrlSet = existingAccounts
            .map { it.profileUrl.trim().lowercase(Locale.US) }
            .toMutableSet()
        var nextAccountId = (existingAccounts.maxOfOrNull { it.id } ?: 0L) + 1L
        val newAccountsToInsert = mutableListOf<AccountBookmarkEntity>()

        for (incoming in payload.accounts) {
            val normalizedProfileUrl = incoming.profileUrl.trim().lowercase(Locale.US)
            if (normalizedProfileUrl in existingAccountUrlSet) continue
            existingAccountUrlSet.add(normalizedProfileUrl)
            val targetFolderId = folderIdMap[incoming.folderId] ?: defaultFolderId
            newAccountsToInsert.add(
                incoming.copy(
                    id = nextAccountId++,
                    folderId = targetFolderId
                )
            )
        }

        return RestorePlan(
            foldersToSave = newFoldersToInsert,
            bookmarksToSave = newBookmarksToInsert,
            accountsToSave = newAccountsToInsert,
            foldersAddedCount = newFoldersToInsert.size,
            bookmarksAddedCount = newBookmarksToInsert.size,
            accountsAddedCount = newAccountsToInsert.size,
            isReplace = false
        )
    }
}
