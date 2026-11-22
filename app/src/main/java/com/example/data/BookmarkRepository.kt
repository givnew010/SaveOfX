package com.example.data

import com.example.util.ParsedTweet
import com.example.util.TweetParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BookmarkRepository(
    private val folderDao: FolderDao,
    private val bookmarkDao: BookmarkDao
) {

    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    val allBookmarks: Flow<List<TweetBookmarkEntity>> = bookmarkDao.getAllBookmarks()

    val favoriteBookmarks: Flow<List<TweetBookmarkEntity>> = bookmarkDao.getFavoriteBookmarks()

    val totalBookmarksCount: Flow<Int> = bookmarkDao.getTotalCount()

    // Combined Flow: Folders with active bookmark counts
    val foldersWithCount: Flow<List<FolderWithCount>> = combine(
        folderDao.getAllFolders(),
        bookmarkDao.getAllBookmarks()
    ) { folders, bookmarks ->
        folders.map { folder ->
            val count = bookmarks.count { it.folderId == folder.id }
            FolderWithCount(folder, count)
        }
    }

    fun getBookmarksByFolder(folderId: Long): Flow<List<TweetBookmarkEntity>> {
        return bookmarkDao.getBookmarksByFolder(folderId)
    }

    fun searchBookmarks(query: String): Flow<List<TweetBookmarkEntity>> {
        return bookmarkDao.searchBookmarks(query)
    }

    suspend fun getFolderById(id: Long): FolderEntity? = folderDao.getFolderById(id)

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folderId: Long, moveToGeneral: Boolean) {
        if (moveToGeneral) {
            val defaultFolder = folderDao.getDefaultFolder()
            val targetId = defaultFolder?.id ?: 1L
            if (targetId != folderId) {
                bookmarkDao.reassignBookmarks(sourceFolderId = folderId, targetFolderId = targetId)
            }
        }
        folderDao.deleteFolderById(folderId)
    }

    suspend fun insertBookmark(bookmark: TweetBookmarkEntity): Long =
        bookmarkDao.insertBookmark(bookmark)

    suspend fun updateBookmark(bookmark: TweetBookmarkEntity) =
        bookmarkDao.updateBookmark(bookmark)

    suspend fun getUnfetchedBookmarks(): List<TweetBookmarkEntity> =
        bookmarkDao.getUnfetchedBookmarksSync()

    suspend fun updateBookmarkWithFetchedTweet(
        existing: TweetBookmarkEntity,
        parsed: ParsedTweet
    ) {
        val serializedMedia = TweetParser.serializeMediaItems(parsed.mediaItems)
        bookmarkDao.updateBookmark(
            existing.copy(
                tweetUrl = parsed.tweetUrl,
                tweetId = parsed.tweetId.ifBlank { existing.tweetId },
                authorName = parsed.authorName.ifBlank { existing.authorName },
                authorHandle = parsed.authorHandle.ifBlank { existing.authorHandle },
                authorAvatarUrl = parsed.authorAvatarUrl ?: existing.authorAvatarUrl,
                content = parsed.content,
                mediaUrl = parsed.mediaUrl,
                mediaType = parsed.mediaType,
                mediaJson = serializedMedia,
                postedAt = parsed.postedAt.ifBlank { existing.postedAt },
                likesCount = parsed.likesCount,
                repostsCount = parsed.repostsCount,
                quoteAuthorName = parsed.quoteAuthorName,
                quoteAuthorHandle = parsed.quoteAuthorHandle,
                quoteText = parsed.quoteText,
                isContentFetched = parsed.isContentFetched
            )
        )
    }

    suspend fun deleteBookmark(id: Long) = bookmarkDao.deleteBookmarkById(id)

    suspend fun toggleFavorite(id: Long, currentFav: Boolean) =
        bookmarkDao.setFavorite(id, !currentFav)

    suspend fun moveBookmark(id: Long, newFolderId: Long) =
        bookmarkDao.moveBookmarkToFolder(id, newFolderId)

    suspend fun getDefaultFolder(): FolderEntity? = folderDao.getDefaultFolder()

    /**
     * Seeds initial folders and sample bookmarks if database is empty.
     */
    suspend fun seedInitialDataIfEmpty() {
        if (folderDao.getFolderCount() == 0) {
            folderDao.insertFolder(
                FolderEntity(
                    name = "العلامات المرجعية العامة",
                    iconEmoji = "📌",
                    colorHex = "#1D9BF0",
                    description = "المجلد الافتراضي لجميع التغريدات غير المصنفة",
                    isDefault = true
                )
            )

            val techId = folderDao.insertFolder(
                FolderEntity(
                    name = "تقنية وبرمجة",
                    iconEmoji = "💻",
                    colorHex = "#3B82F6",
                    description = "شروحات برمجية، أدوات الذكاء الاصطناعي، ومصادر التطوير",
                    isDefault = false
                )
            )

            val inspirationId = folderDao.insertFolder(
                FolderEntity(
                    name = "أفكار وإلهام",
                    iconEmoji = "💡",
                    colorHex = "#F59E0B",
                    description = "اقتباسات مميزة، أفكار مشاريع، وتجارب ملهمة",
                    isDefault = false
                )
            )

            val readLaterId = folderDao.insertFolder(
                FolderEntity(
                    name = "للقراءة والاطلاع",
                    iconEmoji = "📚",
                    colorHex = "#10B981",
                    description = "ثريدات طويلة ومقالات تستحق القراءة المتأنية",
                    isDefault = false
                )
            )

            folderDao.insertFolder(
                FolderEntity(
                    name = "استثمار واقتصاد",
                    iconEmoji = "📈",
                    colorHex = "#8B5CF6",
                    description = "تحليلات مالية، استثمار، واستراتيجيات ريادة الأعمال",
                    isDefault = false
                )
            )

            // Seed sample bookmarks
            if (bookmarkDao.getTotalCountSync() == 0) {
                bookmarkDao.insertBookmark(
                    TweetBookmarkEntity(
                        folderId = techId,
                        tweetUrl = "https://x.com/AndroidDev/status/184512398471923011",
                        tweetId = "184512398471923011",
                        authorName = "Android Developers",
                        authorHandle = "@AndroidDev",
                        content = "Build beautiful, fluid user experiences with Jetpack Compose 🚀 Check out modern navigation and adaptive window size classes in our latest guide.",
                        postedAt = "قبل ساعتين",
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
                        folderId = inspirationId,
                        tweetUrl = "https://x.com/Naval/status/1002103360646823936",
                        tweetId = "1002103360646823936",
                        authorName = "Naval",
                        authorHandle = "@naval",
                        content = "How to Get Rich (without getting lucky):\nSeek wealth, not money or status. Wealth is having assets that earn while you sleep. Money is how we transfer time and wealth.",
                        postedAt = "منذ 3 أيام",
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
                        tags = "ذكاء_اصطناعي,أدوات,انتاجية",
                        userNote = "أدوات مفيدة لتلخيص الاجتماعات وكتابة التقارير",
                        isFavorite = false,
                        likesCount = 3450,
                        repostsCount = 920,
                        isContentFetched = true
                    )
                )
            }
        }
    }
}
