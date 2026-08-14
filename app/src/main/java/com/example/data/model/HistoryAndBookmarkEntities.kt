package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val durationSeconds: Int,
    val lastPositionMs: Long,
    val watchedAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Float
        get() = if (durationSeconds > 0) {
            ((lastPositionMs / 1000f) / durationSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val durationSeconds: Int,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
