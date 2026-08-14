package com.example.data.local

import androidx.room.*
import com.example.data.model.DownloadEntity
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE videoId = :videoId")
    fun getDownloadsForVideo(videoId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET downloadedBytes = :downloadedBytes, totalBytes = :totalBytes, downloadSpeedBytesPerSec = :speed, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, downloadedBytes: Long, totalBytes: Long, speed: Long, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status, localFilePath = :localPath, downloadedBytes = :totalBytes WHERE id = :id")
    suspend fun markCompleted(id: String, localPath: String, totalBytes: Long, status: DownloadStatus = DownloadStatus.COMPLETED)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllHistory(): Flow<List<com.example.data.model.WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getHistoryForVideo(videoId: String): com.example.data.model.WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: com.example.data.model.WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteHistory(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<com.example.data.model.BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE videoId = :videoId)")
    fun isBookmarked(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: com.example.data.model.BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE videoId = :videoId")
    suspend fun removeBookmark(videoId: String)
}
