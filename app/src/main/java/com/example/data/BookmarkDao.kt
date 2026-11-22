package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<TweetBookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE folderId = :folderId ORDER BY savedAt DESC")
    fun getBookmarksByFolder(folderId: Long): Flow<List<TweetBookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteBookmarks(): Flow<List<TweetBookmarkEntity>>

    @Query("""
        SELECT * FROM bookmarks 
        WHERE content LIKE '%' || :query || '%' 
           OR authorName LIKE '%' || :query || '%' 
           OR authorHandle LIKE '%' || :query || '%'
           OR userNote LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY savedAt DESC
    """)
    fun searchBookmarks(query: String): Flow<List<TweetBookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkById(id: Long): TweetBookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE tweetUrl = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): TweetBookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: TweetBookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<TweetBookmarkEntity>): List<Long>

    @Update
    suspend fun updateBookmark(bookmark: TweetBookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: TweetBookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("UPDATE bookmarks SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE bookmarks SET folderId = :newFolderId WHERE id = :id")
    suspend fun moveBookmarkToFolder(id: Long, newFolderId: Long)

    @Query("UPDATE bookmarks SET folderId = :targetFolderId WHERE folderId = :sourceFolderId")
    suspend fun reassignBookmarks(sourceFolderId: Long, targetFolderId: Long)

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getTotalCountSync(): Int

    @Query("SELECT * FROM bookmarks WHERE isContentFetched = 0")
    suspend fun getUnfetchedBookmarksSync(): List<TweetBookmarkEntity>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE folderId = :folderId")
    fun getCountByFolder(folderId: Long): Flow<Int>
}
