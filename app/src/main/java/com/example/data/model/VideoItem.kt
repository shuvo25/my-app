package com.example.data.model

data class VideoQuality(
    val resolution: String, // "1080p", "720p", "480p", "360p", "Audio"
    val url: String,
    val sizeBytes: Long,
    val bitrate: String
)

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String>,
    val durationSeconds: Int,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val uploaderName: String,
    val uploaderAvatar: String,
    val subscribersCount: String,
    val uploadDate: String,
    val thumbnailUrl: String,
    val qualities: List<VideoQuality>,
    val defaultUrl: String = qualities.firstOrNull()?.url ?: ""
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedViews: String
        get() {
            return when {
                views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
                views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
                else -> "$views views"
            }
        }
}
