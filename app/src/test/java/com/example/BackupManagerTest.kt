package com.example

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

        val payload = BackupManager.buildPayload(folders, bookmarks, exportedAt = "2026-11-24T12:00:00.000Z")
        val json = BackupManager.serializePayload(payload)
        val parsed = BackupManager.parseBackupJson(json)

        assertNotNull(parsed)
        assertEquals(2, parsed!!.folders.size)
        assertEquals(1, parsed.bookmarks.size)
        assertEquals("تقنية", parsed.folders[1].name)
        assertEquals("Compose guide", parsed.bookmarks[0].content)
    }

    @Test
    fun parseBackupJson_rejectsInvalidPayloads() {
        assertNull(BackupManager.parseBackupJson(""))
        assertNull(BackupManager.parseBackupJson("{not json"))
        assertNull(BackupManager.parseBackupJson("""{"folders":[],"bookmarks":[]}"""))
    }

    @Test
    fun planRestore_mergeDeduplicatesUrlsAndMapsFolders() {
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
            )
        )

        val plan = BackupManager.planRestore(incoming, RestoreMode.MERGE, existingFolders, existingBookmarks)
        assertEquals(1, plan.foldersAddedCount)
        assertEquals(1, plan.bookmarksAddedCount)
        assertEquals("تصميم", plan.foldersToSave.first().name)
        assertEquals(3L, plan.bookmarksToSave.first().folderId)
        assertTrue(!plan.isReplace)
    }
}
