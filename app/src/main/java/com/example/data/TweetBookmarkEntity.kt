package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "bookmarks",
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
data class TweetBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val folderId: Long,
    val tweetUrl: String,
    val tweetId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val mediaUrl: String? = null,
    val postedAt: String = "",
    val savedAt: Long = System.currentTimeMillis(),
    val userNote: String? = null,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val likesCount: Long = 0L,
    val repostsCount: Long = 0L,
    val isContentFetched: Boolean = false,
    val mediaType: String? = null,
    val mediaJson: String? = null,
    val quoteAuthorName: String? = null,
    val quoteAuthorHandle: String? = null,
    val quoteText: String? = null
)
