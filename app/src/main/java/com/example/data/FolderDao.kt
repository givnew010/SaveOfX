package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query(
        """
        SELECT f.*, COUNT(b.id) AS count
        FROM folders f
        LEFT JOIN bookmarks b ON f.id = b.folderId
        GROUP BY f.id
        ORDER BY f.isDefault DESC, f.createdAt ASC
        """
    )
    fun observeFoldersWithCount(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders ORDER BY isDefault DESC, createdAt ASC")
    suspend fun getAllFoldersOnce(): List<FolderEntity>

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFoldersCount(): Int

    @Query("SELECT * FROM folders WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultFolder(): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>): List<Long>

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}
