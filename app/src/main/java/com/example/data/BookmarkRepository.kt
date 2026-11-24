package com.example.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val folderDao = database.folderDao()
    private val bookmarkDao = database.bookmarkDao()
    private val prefs by lazy {
        context.applicationContext.getSharedPreferences("xfolders_prefs", Context.MODE_PRIVATE)
    }

    val foldersWithCount: Flow<List<FolderWithCount>> = folderDao.observeFoldersWithCount()
    val allBookmarks: Flow<List<TweetBookmarkEntity>> = bookmarkDao.observeAllBookmarks()

    fun getThemeMode(): ThemeMode {
        return ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.key))
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
    }

    fun getLastBackupAt(): String? {
        return prefs.getString(KEY_LAST_BACKUP_AT, null)
    }

    fun markBackupExported(): String {
        val iso = BackupManager.nowIsoTimestamp()
        prefs.edit().putString(KEY_LAST_BACKUP_AT, iso).apply()
        return iso
    }

    suspend fun seedInitialDataIfEmpty() {
        if (folderDao.getFoldersCount() > 0) return

        val now = System.currentTimeMillis()
        val generalId = folderDao.insertFolder(
            FolderEntity(
                name = "العلامات المرجعية العامة",
                iconEmoji = "📌",
                colorHex = "#1D9BF0",
                description = "المجموعة الافتراضية لجميع التغريدات غير المصنفة",
                createdAt = now,
                isDefault = true
            )
        )
        val techId = folderDao.insertFolder(
            FolderEntity(
                name = "تقنية وبرمجة",
                iconEmoji = "💻",
                colorHex = "#3B82F6",
                description = "شروحات برمجية، أدوات الذكاء الاصطناعي، ومصادر التطوير",
                createdAt = now + 1,
                isDefault = false
            )
        )
        val ideasId = folderDao.insertFolder(
            FolderEntity(
                name = "أفكار وإلهام",
                iconEmoji = "💡",
                colorHex = "#F59E0B",
                description = "اقتباسات مميزة، أفكار مشاريع، وتجارب ملهمة",
                createdAt = now + 2,
                isDefault = false
            )
        )
        val readLaterId = folderDao.insertFolder(
            FolderEntity(
                name = "للقراءة والاطلاع",
                iconEmoji = "📚",
                colorHex = "#10B981",
                description = "ثريدات طويلة ومقالات تستحق القراءة المتأنية",
                createdAt = now + 3,
                isDefault = false
            )
        )
        folderDao.insertFolder(
            FolderEntity(
                name = "استثمار واقتصاد",
                iconEmoji = "📈",
                colorHex = "#8B5CF6",
                description = "تحليلات مالية، استثمار، واستراتيجيات ريادة الأعمال",
                createdAt = now + 4,
                isDefault = false
            )
        )

        if (bookmarkDao.getBookmarksCount() == 0) {
            bookmarkDao.insertBookmark(
                TweetBookmarkEntity(
                    folderId = techId,
                    tweetUrl = "https://x.com/AndroidDev/status/184512398471923011",
                    tweetId = "184512398471923011",
                    authorName = "Android Developers",
                    authorHandle = "@AndroidDev",
                    content = "Build beautiful, fluid user experiences with Jetpack Compose 🚀 Check out modern navigation and adaptive window size classes in our latest guide.",
                    postedAt = "قبل ساعتين",
                    savedAt = now - 1000,
                    tags = "android,compose,kotlin",
                    userNote = "مرجع مهم جداً لتصميم واجهات قابلة للتكيف على الأجهزة اللوحية والمطوية",
                    isFavorite = true,
                    likesCount = 1420,
                    repostsCount = 385,
                    isContentFetched = true
                )
            )
            bookmarkDao.insertBookmark(
                TweetBookmarkEntity(
                    folderId = ideasId,
                    tweetUrl = "https://x.com/Naval/status/1002103360646823936",
                    tweetId = "1002103360646823936",
                    authorName = "Naval",
                    authorHandle = "@naval",
                    content = "How to Get Rich (without getting lucky):\nSeek wealth, not money or status. Wealth is having assets that earn while you sleep. Money is how we transfer time and wealth.",
                    postedAt = "منذ 3 أيام",
                    savedAt = now - 2000,
                    tags = "mindset,wealth,wisdom",
                    userNote = "الثريد الشهير لنافال عن صناعة الثروة والرافعة المالية",
                    isFavorite = true,
                    likesCount = 289000,
                    repostsCount = 68400,
                    isContentFetched = true
                )
            )
            bookmarkDao.insertBookmark(
                TweetBookmarkEntity(
                    folderId = readLaterId,
                    tweetUrl = "https://x.com/TechArabic/status/18392019485729104",
                    tweetId = "18392019485729104",
                    authorName = "تقنية بالعربي",
                    authorHandle = "@TechArabic",
                    content = "ثريد شامل: أفضل 10 أدوات مجانية تعمل بالذكاء الاصطناعي لاختصار ساعات العمل اليومية وتلخيص المستندات وإعداد العروض التقديمية 🧵👇",
                    postedAt = "أمس",
                    savedAt = now - 3000,
                    tags = "ذكاء_اصطناعي,أدوات,انتاجية",
                    userNote = "أدوات مفيدة لتلخيص الاجتماعات وكتابة التقارير",
                    isFavorite = false,
                    likesCount = 3450,
                    repostsCount = 920,
                    isContentFetched = true
                )
            )
            // Touch generalId to ensure default group is seeded cleanly
            if (generalId <= 0L) return
        }
    }

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folderId: Long, moveToGeneral: Boolean) {
        if (moveToGeneral) {
            val defaultFolder = folderDao.getDefaultFolder()
            if (defaultFolder != null && defaultFolder.id != folderId) {
                bookmarkDao.moveAllBookmarksBetweenFolders(folderId, defaultFolder.id)
            }
        } else {
            bookmarkDao.deleteBookmarksByFolder(folderId)
        }
        folderDao.deleteFolderById(folderId)
    }

    suspend fun insertBookmark(bookmark: TweetBookmarkEntity): Long = bookmarkDao.insertBookmark(bookmark)

    suspend fun updateBookmark(bookmark: TweetBookmarkEntity) = bookmarkDao.updateBookmark(bookmark)

    suspend fun getUnfetchedBookmarks(): List<TweetBookmarkEntity> = bookmarkDao.getUnfetchedBookmarks()

    suspend fun deleteBookmark(bookmarkId: Long) = bookmarkDao.deleteBookmarkById(bookmarkId)

    suspend fun toggleFavorite(bookmarkId: Long) = bookmarkDao.toggleFavorite(bookmarkId)

    suspend fun moveBookmark(bookmarkId: Long, newFolderId: Long) =
        bookmarkDao.moveBookmark(bookmarkId, newFolderId)

    suspend fun exportBackup(): BackupPayload {
        val folders = folderDao.getAllFoldersOnce()
        val bookmarks = bookmarkDao.getAllBookmarksOnce()
        return BackupManager.buildPayload(folders, bookmarks)
    }

    suspend fun restoreBackup(payload: BackupPayload, mode: RestoreMode): RestoreSummary {
        return database.withTransaction {
            val existingFolders = folderDao.getAllFoldersOnce()
            val existingBookmarks = bookmarkDao.getAllBookmarksOnce()
            val plan = BackupManager.planRestore(payload, mode, existingFolders, existingBookmarks)

            if (plan.isReplace) {
                bookmarkDao.deleteAllBookmarks()
                folderDao.deleteAllFolders()
                folderDao.insertFolders(plan.foldersToSave)
                bookmarkDao.insertBookmarks(plan.bookmarksToSave)
            } else {
                if (plan.foldersToSave.isNotEmpty()) {
                    folderDao.insertFolders(plan.foldersToSave)
                }
                if (plan.bookmarksToSave.isNotEmpty()) {
                    bookmarkDao.insertBookmarks(plan.bookmarksToSave)
                }
            }

            RestoreSummary(
                foldersAdded = plan.foldersAddedCount,
                bookmarksAdded = plan.bookmarksAddedCount
            )
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_BACKUP_AT = "last_backup_at"
    }
}
