package com.example.data

data class FolderWithCount(
    val folder: FolderEntity,
    val count: Int
)

data class BookmarkWithFolder(
    val bookmark: TweetBookmarkEntity,
    val folder: FolderEntity?
)
