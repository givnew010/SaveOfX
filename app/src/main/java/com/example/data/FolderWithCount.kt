package com.example.data

import androidx.room.Embedded

data class FolderWithCount(
    @Embedded val folder: FolderEntity,
    val count: Int
)
