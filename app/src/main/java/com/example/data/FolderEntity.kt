package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconEmoji: String = "📁",
    val colorHex: String = "#1D9BF0",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
