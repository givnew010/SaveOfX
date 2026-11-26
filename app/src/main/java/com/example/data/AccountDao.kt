package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY savedAt DESC")
    fun observeAllAccounts(): Flow<List<AccountBookmarkEntity>>

    @Query("SELECT * FROM accounts ORDER BY savedAt DESC")
    suspend fun getAllAccountsOnce(): List<AccountBookmarkEntity>

    @Query("SELECT * FROM accounts WHERE isProfileFetched = 0 ORDER BY savedAt DESC")
    suspend fun getUnfetchedAccounts(): List<AccountBookmarkEntity>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountBookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountBookmarkEntity>)

    @Update
    suspend fun updateAccount(account: AccountBookmarkEntity)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: Long)

    @Query("UPDATE accounts SET isFavorite = NOT isFavorite WHERE id = :accountId")
    suspend fun toggleFavorite(accountId: Long)

    @Query("UPDATE accounts SET folderId = :newFolderId WHERE id = :accountId")
    suspend fun moveAccount(accountId: Long, newFolderId: Long)

    @Query("UPDATE accounts SET folderId = :targetFolderId WHERE folderId = :oldFolderId")
    suspend fun moveAllAccountsBetweenFolders(oldFolderId: Long, targetFolderId: Long)

    @Query("DELETE FROM accounts WHERE folderId = :folderId")
    suspend fun deleteAccountsByFolder(folderId: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
}
