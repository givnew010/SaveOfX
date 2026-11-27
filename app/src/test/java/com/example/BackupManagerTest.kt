package com.example

import com.example.data.AccountBookmarkEntity
import com.example.data.BackupManager
import com.example.data.FolderEntity
import com.example.data.PersonEntity
import com.example.data.RestoreMode
import com.example.data.TweetBookmarkEntity
import com.example.util.PersonWorkCodec
import com.example.util.PersonWorkItem
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
        val people = listOf(
            PersonEntity(
                id = 30L,
                folderId = 2L,
                name = "عمر الشريف",
                role = "ممثل",
                bio = "ممثل عالمي",
                worksJson = PersonWorkCodec.serializeWorks(
                    listOf(
                        PersonWorkItem(
                            id = "w1",
                            type = "link",
                            title = "IMDb",
                            url = "https://www.imdb.com/name/nm0001725/"
                        )
                    )
                )
            )
        )

        val payload = BackupManager.buildPayload(
            folders = folders,
            bookmarks = bookmarks,
            accounts = accounts,
            people = people,
            exportedAt = "2026-11-24T12:00:00.000Z"
        )
        val json = BackupManager.serializePayload(payload)
        val parsed = BackupManager.parseBackupJson(json)

        assertNotNull(parsed)
        assertEquals(2, parsed!!.folders.size)
        assertEquals(1, parsed.bookmarks.size)
        assertEquals(1, parsed.accounts.size)
        assertEquals(1, parsed.people.size)
        assertEquals("تقنية", parsed.folders[1].name)
        assertEquals("Compose guide", parsed.bookmarks[0].content)
        assertEquals("@AndroidDev", parsed.accounts[0].handle)
        assertEquals("عمر الشريف", parsed.people[0].name)
        assertEquals(1, PersonWorkCodec.parseWorks(parsed.people[0].worksJson).size)
    }

    @Test
    fun parseBackupJson_acceptsWebWorksArrayFormat() {
        val webBackupJson = """
            {
              "app": "X Folders",
              "version": 3,
              "exportedAt": "2026-11-26T10:00:00.000Z",
              "folders": [{"id": 1, "name": "العامة", "isDefault": true}],
              "bookmarks": [],
              "accounts": [],
              "people": [
                {
                  "id": 9,
                  "folderId": 1,
                  "name": "أحمد حلمي",
                  "role": "ممثل",
                  "works": [
                    {"id": "w_web", "type": "photo", "title": "ملصق", "url": "https://example.com/poster.jpg", "addedAt": 1700000000000}
                  ]
                }
              ]
            }
        """.trimIndent()

        val parsed = BackupManager.parseBackupJson(webBackupJson)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.people.size)
        val parsedWorks = PersonWorkCodec.parseWorks(parsed.people[0].worksJson)
        assertEquals(1, parsedWorks.size)
        assertEquals("photo", parsedWorks[0].type)
    }

    @Test
    fun parseBackupJson_rejectsInvalidPayloads() {
        assertNull(BackupManager.parseBackupJson(""))
        assertNull(BackupManager.parseBackupJson("{not json"))
        assertNull(BackupManager.parseBackupJson("""{"folders":[],"bookmarks":[]}"""))
    }

    @Test
    fun planRestore_mergeDeduplicatesUrlsAccountsAndPeopleAndMapsFolders() {
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
        val existingPeople = listOf(
            PersonEntity(
                id = 1L,
                folderId = 2L,
                name = "كريستوفر نولان",
                role = "مخرج"
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
            ),
            people = listOf(
                PersonEntity(
                    id = 40L,
                    folderId = 5L,
                    name = "كريستوفر نولان",
                    role = "مخرج"
                ),
                PersonEntity(
                    id = 41L,
                    folderId = 6L,
                    name = "عمر الشريف",
                    role = "ممثل"
                )
            )
        )

        val plan = BackupManager.planRestore(
            payload = incoming,
            mode = RestoreMode.MERGE,
            existingFolders = existingFolders,
            existingBookmarks = existingBookmarks,
            existingAccounts = existingAccounts,
            existingPeople = existingPeople
        )
        assertEquals(1, plan.foldersAddedCount)
        assertEquals(1, plan.bookmarksAddedCount)
        assertEquals(1, plan.accountsAddedCount)
        assertEquals(1, plan.peopleAddedCount)
        assertEquals("تصميم", plan.foldersToSave.first().name)
        assertEquals(3L, plan.bookmarksToSave.first().folderId)
        assertEquals(3L, plan.accountsToSave.first().folderId)
        assertEquals(3L, plan.peopleToSave.first().folderId)
        assertEquals("@karpathy", plan.accountsToSave.first().handle)
        assertEquals("عمر الشريف", plan.peopleToSave.first().name)
        assertTrue(!plan.isReplace)
    }
}
