package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [
        Index(value = ["folderId"]),
        Index(value = ["tweetUrl"])
    ]
)
data class TweetBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long,
    val tweetUrl: String,
    val tweetId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val mediaUrl: String? = null,
    val postedAt: String = "",
    val savedAt: Long = System.currentTimeMillis(),
    val userNote: String? = null,
    val tags: String = "", // Comma-separated list of tags
    val isFavorite: Boolean = false,
    val likesCount: Int = 0,
    val repostsCount: Int = 0,
    val isContentFetched: Boolean = false,
    val mediaType: String? = null, // "photo", "video", "gif"
    val mediaJson: String? = null, // Serialized list of TweetMediaItem
    val quoteAuthorName: String? = null,
    val quoteAuthorHandle: String? = null,
    val quoteText: String? = null
)
