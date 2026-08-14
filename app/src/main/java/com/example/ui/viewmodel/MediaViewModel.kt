package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.downloader.VideoDownloadManager
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab {
    HOME,
    SEARCH,
    DOWNLOADS,
    SAVED
}

data class PlayerState(
    val currentVideo: VideoItem? = null,
    val selectedQuality: VideoQuality? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val bufferedPercentage: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isFullscreen: Boolean = false,
    val isAudioOnly: Boolean = false,
    val isControlsVisible: Boolean = true,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)

class MediaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val downloadManager = VideoDownloadManager(application, database.downloadDao())
    val repository = VideoRepository(
        database.downloadDao(),
        database.historyDao(),
        database.bookmarkDao()
    )

    // Navigation State
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Active Category Filter
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Video Catalog
    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList.asStateFlow()

    // Search Query & Results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val searchResults: StateFlow<List<VideoItem>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    // Player State
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Active Video Detail screen (null if browsing feeds)
    private val _selectedDetailVideo = MutableStateFlow<VideoItem?>(null)
    val selectedDetailVideo: StateFlow<VideoItem?> = _selectedDetailVideo.asStateFlow()

    // Quality Picker Modal for Video Download or Player
    private val _downloadModalVideo = MutableStateFlow<VideoItem?>(null)
    val downloadModalVideo: StateFlow<VideoItem?> = _downloadModalVideo.asStateFlow()

    // Add Stream Dialog
    private val _isAddStreamDialogVisible = MutableStateFlow(false)
    val isAddStreamDialogVisible: StateFlow<Boolean> = _isAddStreamDialogVisible.asStateFlow()

    // Room DB Observables
    val activeDownloads: StateFlow<List<DownloadEntity>> = database.downloadDao()
        .getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadEntity>> = database.downloadDao()
        .getCompletedDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.getWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarked check for active detail video
    val isCurrentVideoBookmarked: StateFlow<Boolean> = _selectedDetailVideo
        .flatMapLatest { video ->
            if (video != null) repository.isBookmarked(video.id) else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadVideos()
    }

    fun setNavTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _videoList.value = repository.getVideosByCategory(category)
    }

    fun loadVideos() {
        _videoList.value = repository.getVideosByCategory(_selectedCategory.value)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250) // Debounce 250ms
            _searchResults.value = repository.searchVideos(query, _selectedCategory.value)
        }
    }

    fun openVideoDetail(video: VideoItem) {
        _selectedDetailVideo.value = video
        viewModelScope.launch {
            val lastPos = repository.getLastPosition(video.id)
            val defaultQuality = video.qualities.firstOrNull()
            _playerState.value = PlayerState(
                currentVideo = video,
                selectedQuality = defaultQuality,
                isPlaying = true,
                currentPositionMs = lastPos,
                totalDurationMs = video.durationSeconds * 1000L,
                isControlsVisible = true
            )
        }
    }

    fun closeVideoDetail() {
        val current = _playerState.value.currentVideo
        val pos = _playerState.value.currentPositionMs
        if (current != null) {
            viewModelScope.launch {
                repository.saveWatchProgress(current, pos)
            }
        }
        _playerState.value = _playerState.value.copy(isPlaying = false)
        _selectedDetailVideo.value = null
    }

    fun playOfflineVideo(download: DownloadEntity) {
        val video = VideoItem(
            id = download.videoId,
            title = download.title,
            description = "Offline downloaded media playback",
            category = "Downloads",
            tags = listOf("offline", "download"),
            durationSeconds = 600,
            views = 1L,
            likes = 0L,
            dislikes = 0L,
            uploaderName = "Offline Storage",
            uploaderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "Local",
            uploadDate = "Downloaded",
            thumbnailUrl = download.thumbnailUrl,
            qualities = listOf(
                VideoQuality(
                    resolution = download.quality,
                    url = download.localFilePath ?: download.downloadUrl,
                    sizeBytes = download.totalBytes,
                    bitrate = "Local"
                )
            )
        )
        openVideoDetail(video)
    }

    fun togglePlayPause() {
        _playerState.value = _playerState.value.copy(isPlaying = !_playerState.value.isPlaying)
    }

    fun updatePlaybackPosition(positionMs: Long, durationMs: Long, bufferPercent: Int = 0) {
        _playerState.value = _playerState.value.copy(
            currentPositionMs = positionMs,
            totalDurationMs = if (durationMs > 0) durationMs else _playerState.value.totalDurationMs,
            bufferedPercentage = bufferPercent
        )
    }

    fun seekTo(positionMs: Long) {
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun selectQuality(quality: VideoQuality) {
        _playerState.value = _playerState.value.copy(
            selectedQuality = quality,
            isBuffering = true
        )
    }

    fun toggleFullscreen() {
        _playerState.value = _playerState.value.copy(isFullscreen = !_playerState.value.isFullscreen)
    }

    fun toggleControlsVisibility() {
        _playerState.value = _playerState.value.copy(isControlsVisible = !_playerState.value.isControlsVisible)
    }

    fun toggleAudioOnly() {
        _playerState.value = _playerState.value.copy(isAudioOnly = !_playerState.value.isAudioOnly)
    }

    fun toggleBookmarkCurrentVideo() {
        val video = _selectedDetailVideo.value ?: return
        val currentBookmarked = isCurrentVideoBookmarked.value
        viewModelScope.launch {
            repository.toggleBookmark(video, currentBookmarked)
        }
    }

    // Downloader Actions
    fun openDownloadModal(video: VideoItem) {
        _downloadModalVideo.value = video
    }

    fun closeDownloadModal() {
        _downloadModalVideo.value = null
    }

    fun startDownload(video: VideoItem, quality: VideoQuality) {
        downloadManager.startDownload(video, quality)
        closeDownloadModal()
    }

    fun pauseDownload(id: String) {
        downloadManager.pauseDownload(id)
    }

    fun resumeDownload(id: String) {
        downloadManager.resumeDownload(id)
    }

    fun deleteDownload(id: String) {
        downloadManager.cancelOrDeleteDownload(id)
    }

    // Add Stream Dialog
    fun showAddStreamDialog() {
        _isAddStreamDialogVisible.value = true
    }

    fun hideAddStreamDialog() {
        _isAddStreamDialogVisible.value = false
    }

    fun addCustomStream(title: String, url: String, category: String) {
        val added = repository.addCustomStream(title, url, category)
        _isAddStreamDialogVisible.value = false
        loadVideos()
        openVideoDetail(added)
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        val current = _playerState.value.currentVideo
        val pos = _playerState.value.currentPositionMs
        if (current != null) {
            viewModelScope.launch {
                repository.saveWatchProgress(current, pos)
            }
        }
    }
}
