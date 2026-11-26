package com.example

import com.example.data.AccountBookmarkEntity
import com.example.data.BackupManager
import com.example.data.FolderEntity
import com.example.data.RestoreMode
import com.example.data.TweetBookmarkEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {
    @Test
    fun exportAndParseBackup_roundTripsCorrectly() {
        val folders = listOf(
            FolderEntity(id = 1L, name = "العامة", iconEmoji = "📌", isDefault = true),
            FolderEntity(id = 2L, name = "تقنية", iconEmoji = "💻", isDefault = false)
        )
        val bookmarks = listOf(
            TweetBookmarkEntity(
                id = 10L,
                folderId = 2L,
                tweetUrl = "https://x.com/AndroidDev/status/184512398471923011",
                tweetId = "184512398471923011",
                authorName = "Android Developers",
                authorHandle = "@AndroidDev",
                content = "Compose guide",
                isContentFetched = true
            )
        )
        val accounts = listOf(
            AccountBookmarkEntity(
                id = 20L,
                folderId = 2L,
                profileUrl = "https://x.com/AndroidDev",
                handle = "@AndroidDev",
                displayName = "Android Developers",
                bio = "Official Android dev account",
                followersCount = 2150000L,
                verified = true,
                isProfileFetched = true
            )
        )

        val payload = BackupManager.buildPayload(
            folders = folders,
            bookmarks = bookmarks,
            accounts = accounts,
            exportedAt = "2026-11-24T12:00:00.000Z"
        )
        val json = BackupManager.serializePayload(payload)
        val parsed = BackupManager.parseBackupJson(json)

        assertNotNull(parsed)
        assertEquals(2, parsed!!.folders.size)
        assertEquals(1, parsed.bookmarks.size)
        assertEquals(1, parsed.accounts.size)
        assertEquals("تقنية", parsed.folders[1].name)
        assertEquals("Compose guide", parsed.bookmarks[0].content)
        assertEquals("@AndroidDev", parsed.accounts[0].handle)
    }

    @Test
    fun parseBackupJson_rejectsInvalidPayloads() {
        assertNull(BackupManager.parseBackupJson(""))
        assertNull(BackupManager.parseBackupJson("{not json"))
        assertNull(BackupManager.parseBackupJson("""{"folders":[],"bookmarks":[]}"""))
    }

    @Test
    fun planRestore_mergeDeduplicatesUrlsAndAccountsAndMapsFolders() {
        val existingFolders = listOf(
            FolderEntity(id = 1L, name = "العامة", isDefault = true),
            FolderEntity(id = 2L, name = "تقنية", isDefault = false)
        )
        val existingBookmarks = listOf(
            TweetBookmarkEntity(
                id = 1L,
                folderId = 2L,
                tweetUrl = "https://x.com/user/status/111",
                tweetId = "111",
                authorName = "User",
                authorHandle = "@user",
                content = "Existing"
            )
        )
        val existingAccounts = listOf(
            AccountBookmarkEntity(
                id = 1L,
                folderId = 2L,
                profileUrl = "https://x.com/AndroidDev",
                handle = "@AndroidDev",
                displayName = "Android Developers"
            )
        )

        val incoming = BackupManager.buildPayload(
            folders = listOf(
                FolderEntity(id = 5L, name = "تقنية", isDefault = false),
                FolderEntity(id = 6L, name = "تصميم", isDefault = false)
            ),
            bookmarks = listOf(
                TweetBookmarkEntity(
                    id = 20L,
                    folderId = 5L,
                    tweetUrl = "https://x.com/user/status/111",
                    tweetId = "111",
                    authorName = "User",
                    authorHandle = "@user",
                    content = "Duplicate"
                ),
                TweetBookmarkEntity(
                    id = 21L,
                    folderId = 6L,
                    tweetUrl = "https://x.com/user/status/222",
                    tweetId = "222",
                    authorName = "Designer",
                    authorHandle = "@designer",
                    content = "New tweet"
                )
            ),
            accounts = listOf(
                AccountBookmarkEntity(
                    id = 30L,
                    folderId = 5L,
                    profileUrl = "https://x.com/androiddev",
                    handle = "@AndroidDev",
                    displayName = "Duplicate Account"
                ),
                AccountBookmarkEntity(
                    id = 31L,
                    folderId = 6L,
                    profileUrl = "https://x.com/karpathy",
                    handle = "@karpathy",
                    displayName = "Andrej Karpathy"
                )
            )
        )

        val plan = BackupManager.planRestore(
            payload = incoming,
            mode = RestoreMode.MERGE,
            existingFolders = existingFolders,
            existingBookmarks = existingBookmarks,
            existingAccounts = existingAccounts
        )
        assertEquals(1, plan.foldersAddedCount)
        assertEquals(1, plan.bookmarksAddedCount)
        assertEquals(1, plan.accountsAddedCount)
        assertEquals("تصميم", plan.foldersToSave.first().name)
        assertEquals(3L, plan.bookmarksToSave.first().folderId)
        assertEquals(3L, plan.accountsToSave.first().folderId)
        assertEquals("@karpathy", plan.accountsToSave.first().handle)
        assertTrue(!plan.isReplace)
    }
}
