package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val folderId: Long,
    val name: String,
    val role: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val userNote: String? = null,
    val tags: String = "",
    val worksJson: String = "[]",
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
