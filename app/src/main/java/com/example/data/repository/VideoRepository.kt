package com.example.data.repository

import com.example.data.local.BookmarkDao
import com.example.data.local.DownloadDao
import com.example.data.local.HistoryDao
import com.example.data.model.*
import com.example.data.remote.SupabaseNetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class VideoRepository(
    private val downloadDao: DownloadDao,
    private val historyDao: HistoryDao,
    private val bookmarkDao: BookmarkDao
) {
    private val _customVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    // Curated high quality media catalog with diverse resolutions
    private val defaultVideos = listOf(
        VideoItem(
            id = "vid_1",
            title = "Big Buck Bunny - 4K 60FPS Ultra HDR",
            description = "A large and lovable rabbit deals with bullying forest creatures in this iconic open-source animation masterpiece by the Blender Foundation.",
            category = "Animation",
            tags = listOf("animation", "4k", "blender", "nature", "trending"),
            durationSeconds = 596,
            views = 14205000L,
            likes = 890000L,
            dislikes = 12000L,
            uploaderName = "Blender Open Movies",
            uploaderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "4.2M",
            uploadDate = "3 days ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 158 * 1024 * 1024L, "8.5 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 82 * 1024 * 1024L, "4.2 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 45 * 1024 * 1024L, "2.1 Mbps"),
                VideoQuality("360p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 22 * 1024 * 1024L, "1.0 Mbps"),
                VideoQuality("Audio", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 8 * 1024 * 1024L, "192 kbps")
            )
        ),
        VideoItem(
            id = "vid_2",
            title = "Tears of Steel - Cyberpunk Sci-Fi VFX Showcase",
            description = "Set in a dystopian future in Amsterdam, a group of warriors and scientists try to save humanity with robotic prosthetic technology and neural links.",
            category = "Cinematic",
            tags = listOf("cyberpunk", "scifi", "cinematic", "vfx", "trending"),
            durationSeconds = 734,
            views = 8450000L,
            likes = 620000L,
            dislikes = 8500L,
            uploaderName = "Mango Open VFX Studio",
            uploaderAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "1.8M",
            uploadDate = "1 week ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", 195 * 1024 * 1024L, "9.2 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", 110 * 1024 * 1024L, "4.8 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", 55 * 1024 * 1024L, "2.4 Mbps"),
                VideoQuality("360p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", 28 * 1024 * 1024L, "1.2 Mbps")
            )
        ),
        VideoItem(
            id = "vid_3",
            title = "Sintel - Fantasy Dragon Quest & Epic Journey",
            description = "A lonely young girl named Sintel discovers a wounded baby dragon, nurtures it back to health, and embarks on a dangerous mountain quest.",
            category = "Animation",
            tags = listOf("fantasy", "animation", "dragon", "action", "cinematic"),
            durationSeconds = 888,
            views = 19800000L,
            likes = 1250000L,
            dislikes = 15000L,
            uploaderName = "Durian Open Art",
            uploaderAvatar = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "3.1M",
            uploadDate = "2 weeks ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", 140 * 1024 * 1024L, "7.8 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", 75 * 1024 * 1024L, "3.9 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", 38 * 1024 * 1024L, "1.9 Mbps"),
                VideoQuality("360p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", 18 * 1024 * 1024L, "0.9 Mbps")
            )
        ),
        VideoItem(
            id = "vid_4",
            title = "Elephants Dream - Surreal Mechanized Worlds",
            description = "Explore the labyrinthine mechanics of a gigantic living machine controlled by electric conduits and surreal clockwork chambers.",
            category = "Tech",
            tags = listOf("surreal", "tech", "futuristic", "blender", "sci-fi"),
            durationSeconds = 654,
            views = 5600000L,
            likes = 430000L,
            dislikes = 6000L,
            uploaderName = "Orange Cinema Labs",
            uploaderAvatar = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "920K",
            uploadDate = "3 weeks ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", 125 * 1024 * 1024L, "6.8 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", 68 * 1024 * 1024L, "3.4 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", 34 * 1024 * 1024L, "1.7 Mbps")
            )
        ),
        VideoItem(
            id = "vid_5",
            title = "Chromecast Blazes - Fast 4K HDR Speed Trail",
            description = "Ultra high frame rate speed tests capturing extreme fire acceleration and high-velocity trail rendering with deep contrast dynamics.",
            category = "Trending",
            tags = listOf("4k", "hdr", "trending", "speed", "test"),
            durationSeconds = 15,
            views = 28400000L,
            likes = 1920000L,
            dislikes = 21000L,
            uploaderName = "Pixel Foundry",
            uploaderAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "6.5M",
            uploadDate = "Yesterday",
            thumbnailUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", 15 * 1024 * 1024L, "12.0 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", 8 * 1024 * 1024L, "6.0 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", 4 * 1024 * 1024L, "3.0 Mbps")
            )
        ),
        VideoItem(
            id = "vid_6",
            title = "Submerged Ocean Odyssey - Deep Coral Reef 8K",
            description = "Dive into underwater coral ecosystems with bioluminescent marine life, manta rays, and pristine oceanic trenches.",
            category = "Nature",
            tags = listOf("nature", "ocean", "animals", "4k", "relaxing"),
            durationSeconds = 480,
            views = 11200000L,
            likes = 780000L,
            dislikes = 4500L,
            uploaderName = "AquaPlanet Media",
            uploaderAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "2.4M",
            uploadDate = "5 days ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1682687220063-4742bd7fd538?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", 95 * 1024 * 1024L, "8.0 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", 50 * 1024 * 1024L, "4.0 Mbps"),
                VideoQuality("480p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", 25 * 1024 * 1024L, "2.0 Mbps")
            )
        ),
        VideoItem(
            id = "vid_7",
            title = "Synthwave Nightfall - Cyber Electronic Beats",
            description = "Immersive retro 80s visualizer with synthesizer melodies, neon grid horizons, and driving synthwave vibes for focus and gaming.",
            category = "Music",
            tags = listOf("music", "synthwave", "beats", "cyberpunk", "relax"),
            durationSeconds = 360,
            views = 7600000L,
            likes = 540000L,
            dislikes = 3200L,
            uploaderName = "Neon Frequency",
            uploaderAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "1.5M",
            uploadDate = "4 days ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", 80 * 1024 * 1024L, "7.0 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", 42 * 1024 * 1024L, "3.5 Mbps"),
                VideoQuality("Audio", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", 12 * 1024 * 1024L, "320 kbps")
            )
        ),
        VideoItem(
            id = "vid_8",
            title = "Next-Gen Unreal Engine 5.5 Raytracing Benchmark",
            description = "Comprehensive stress test showcasing Nanite virtualized geometry, Lumen real-time global illumination, and Substrate material shaders.",
            category = "Gaming",
            tags = listOf("gaming", "unreal", "tech", "graphics", "benchmark"),
            durationSeconds = 620,
            views = 9800000L,
            likes = 810000L,
            dislikes = 7800L,
            uploaderName = "TechVortex Labs",
            uploaderAvatar = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "3.8M",
            uploadDate = "6 days ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4", 115 * 1024 * 1024L, "8.5 Mbps"),
                VideoQuality("720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4", 60 * 1024 * 1024L, "4.2 Mbps")
            )
        )
    )

    val categories = listOf(
        "All",
        "Trending",
        "Cinematic",
        "Animation",
        "Music",
        "Gaming",
        "Tech",
        "Nature"
    )

    fun getAllVideos(): List<VideoItem> {
        return _customVideos.value + defaultVideos
    }

    fun getVideoById(id: String): VideoItem? {
        return getAllVideos().find { it.id == id }
    }

    fun getVideosByCategory(category: String): List<VideoItem> {
        val all = getAllVideos()
        return if (category == "All") {
            all
        } else if (category == "Trending") {
            all.sortedByDescending { it.views }
        } else {
            all.filter { it.category.equals(category, ignoreCase = true) || it.tags.any { tag -> tag.equals(category, ignoreCase = true) } }
        }
    }

    fun searchVideos(query: String, category: String = "All"): List<VideoItem> {
        val base = if (category == "All") getAllVideos() else getVideosByCategory(category)
        if (query.isBlank()) return base

        val q = query.trim().lowercase()
        return base.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.uploaderName.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }

    fun getRelatedVideos(videoId: String): List<VideoItem> {
        val current = getVideoById(videoId) ?: return defaultVideos.take(4)
        return getAllVideos()
            .filter { it.id != videoId }
            .sortedByDescending { it.category == current.category }
            .take(6)
    }

    fun addCustomStream(
        title: String,
        streamUrl: String,
        category: String,
        description: String = "Custom user imported stream"
    ): VideoItem {
        val newId = "custom_${System.currentTimeMillis()}"
        val newVideo = VideoItem(
            id = newId,
            title = title.ifBlank { "Custom Live Stream" },
            description = description,
            category = category.ifBlank { "Trending" },
            tags = listOf("custom", "stream", category.lowercase()),
            durationSeconds = 300,
            views = 1L,
            likes = 1L,
            dislikes = 0L,
            uploaderName = "User Import",
            uploaderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop&q=80",
            subscribersCount = "1",
            uploadDate = "Just now",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            qualities = listOf(
                VideoQuality("Auto", streamUrl, 50 * 1024 * 1024L, "Adaptive"),
                VideoQuality("1080p", streamUrl, 50 * 1024 * 1024L, "Original"),
                VideoQuality("720p", streamUrl, 30 * 1024 * 1024L, "Medium")
            )
        )
        _customVideos.value = listOf(newVideo) + _customVideos.value
        return newVideo
    }

    // Room DB History operations
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = historyDao.getAllHistory()

    suspend fun saveWatchProgress(video: VideoItem, positionMs: Long) {
        if (positionMs > 1000) {
            historyDao.recordHistory(
                WatchHistoryEntity(
                    videoId = video.id,
                    title = video.title,
                    thumbnailUrl = video.thumbnailUrl,
                    uploaderName = video.uploaderName,
                    durationSeconds = video.durationSeconds,
                    lastPositionMs = positionMs,
                    watchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getLastPosition(videoId: String): Long {
        return historyDao.getHistoryForVideo(videoId)?.lastPositionMs ?: 0L
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    // Bookmarks
    fun isBookmarked(videoId: String): Flow<Boolean> = bookmarkDao.isBookmarked(videoId)

    suspend fun toggleBookmark(video: VideoItem, isCurrentlyBookmarked: Boolean, userSession: UserSession? = null) {
        if (isCurrentlyBookmarked) {
            bookmarkDao.removeBookmark(video.id)
            if (userSession?.accessToken != null && !userSession.isDemoAccount) {
                try {
                    SupabaseNetworkClient.restService.deleteBookmark(
                        authHeader = "Bearer ${userSession.accessToken}",
                        videoId = "eq.${video.id}",
                        userId = "eq.${userSession.id}"
                    )
                } catch (e: Exception) {
                    // Ignore network failure; Room DB is the local source of truth
                }
            }
        } else {
            val entity = BookmarkEntity(
                videoId = video.id,
                title = video.title,
                thumbnailUrl = video.thumbnailUrl,
                uploaderName = video.uploaderName,
                durationSeconds = video.durationSeconds,
                bookmarkedAt = System.currentTimeMillis()
            )
            bookmarkDao.insertBookmark(entity)

            if (userSession?.accessToken != null && !userSession.isDemoAccount) {
                try {
                    val dto = RemoteBookmarkDto(
                        userId = userSession.id,
                        videoId = video.id,
                        title = video.title,
                        thumbnailUrl = video.thumbnailUrl,
                        uploaderName = video.uploaderName,
                        durationSeconds = video.durationSeconds
                    )
                    SupabaseNetworkClient.restService.addBookmark(
                        authHeader = "Bearer ${userSession.accessToken}",
                        bookmark = dto
                    )
                } catch (e: Exception) {
                    // Ignore network failure
                }
            }
        }
    }

    suspend fun syncBookmarksWithCloud(userSession: UserSession) {
        if (userSession.isDemoAccount || userSession.accessToken == null) return
        _isCloudSyncing.value = true
        try {
            val response = SupabaseNetworkClient.restService.getBookmarks(
                authHeader = "Bearer ${userSession.accessToken}",
                userIdFilter = "eq.${userSession.id}"
            )
            if (response.isSuccessful) {
                val remoteList = response.body().orEmpty()
                for (remote in remoteList) {
                    bookmarkDao.insertBookmark(
                        BookmarkEntity(
                            videoId = remote.videoId,
                            title = remote.title,
                            thumbnailUrl = remote.thumbnailUrl,
                            uploaderName = remote.uploaderName,
                            durationSeconds = remote.durationSeconds,
                            bookmarkedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore offline errors
        } finally {
            _isCloudSyncing.value = false
        }
    }

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
}

