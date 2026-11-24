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
    fun observeAllBookmarks(): Flow<List<TweetBookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    suspend fun getAllBookmarksOnce(): List<TweetBookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE isContentFetched = 0 ORDER BY savedAt DESC")
    suspend fun getUnfetchedBookmarks(): List<TweetBookmarkEntity>

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarksCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: TweetBookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<TweetBookmarkEntity>)

    @Update
    suspend fun updateBookmark(bookmark: TweetBookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: TweetBookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("UPDATE bookmarks SET isFavorite = NOT isFavorite WHERE id = :bookmarkId")
    suspend fun toggleFavorite(bookmarkId: Long)

    @Query("UPDATE bookmarks SET folderId = :newFolderId WHERE id = :bookmarkId")
    suspend fun moveBookmark(bookmarkId: Long, newFolderId: Long)

    @Query("UPDATE bookmarks SET folderId = :targetFolderId WHERE folderId = :oldFolderId")
    suspend fun moveAllBookmarksBetweenFolders(oldFolderId: Long, targetFolderId: Long)

    @Query("DELETE FROM bookmarks WHERE folderId = :folderId")
    suspend fun deleteBookmarksByFolder(folderId: Long)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}
