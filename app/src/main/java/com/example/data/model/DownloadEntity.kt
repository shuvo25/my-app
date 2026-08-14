package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // videoId + "_" + quality
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val downloadUrl: String,
    val quality: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val localFilePath: String? = null,
    val downloadSpeedBytesPerSec: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()

    val formattedDownloadedSize: String
        get() = formatFileSize(downloadedBytes)

    val formattedTotalSize: String
        get() = if (totalBytes > 0) formatFileSize(totalBytes) else "--"

    val formattedSpeed: String
        get() = "${formatFileSize(downloadSpeedBytesPerSec)}/s"

    val etaSeconds: Long
        get() {
            if (downloadSpeedBytesPerSec <= 0 || totalBytes <= downloadedBytes) return 0L
            return (totalBytes - downloadedBytes) / downloadSpeedBytesPerSec
        }

    val formattedEta: String
        get() {
            val eta = etaSeconds
            return if (eta > 60) "${eta / 60}m left" else "${eta}s left"
        }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
