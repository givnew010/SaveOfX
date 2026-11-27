package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY savedAt DESC")
    fun observeAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people ORDER BY savedAt DESC")
    suspend fun getAllPeopleOnce(): List<PersonEntity>

    @Query("SELECT COUNT(*) FROM people")
    suspend fun getPeopleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("DELETE FROM people WHERE id = :personId")
    suspend fun deletePersonById(personId: Long)

    @Query("UPDATE people SET isFavorite = NOT isFavorite WHERE id = :personId")
    suspend fun toggleFavorite(personId: Long)

    @Query("UPDATE people SET folderId = :newFolderId WHERE id = :personId")
    suspend fun movePerson(personId: Long, newFolderId: Long)

    @Query("UPDATE people SET folderId = :targetFolderId WHERE folderId = :oldFolderId")
    suspend fun moveAllPeopleBetweenFolders(oldFolderId: Long, targetFolderId: Long)

    @Query("DELETE FROM people WHERE folderId = :folderId")
    suspend fun deletePeopleByFolder(folderId: Long)

    @Query("DELETE FROM people")
    suspend fun deleteAllPeople()
}
