package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "accounts",
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
data class AccountBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val folderId: Long,
    val profileUrl: String,
    val handle: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val verified: Boolean = false,
    val userNote: String? = null,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis(),
    val isProfileFetched: Boolean = false
)
