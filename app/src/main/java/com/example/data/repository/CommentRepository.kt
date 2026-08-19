package com.example.data.repository

import com.example.data.model.CommentItem
import com.example.data.model.RemoteCommentDto
import com.example.data.model.UserSession
import com.example.data.remote.SupabaseNetworkClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class CommentRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // In-memory cache of comments per video
    private val _commentsMap = MutableStateFlow<Map<String, List<CommentItem>>>(emptyMap())

    init {
        seedDefaultComments()
    }

    private fun seedDefaultComments() {
        val initialComments = mutableMapOf<String, List<CommentItem>>()

        // Comments for Big Buck Bunny (vid_1)
        initialComments["vid_1"] = listOf(
            CommentItem(
                id = "c_101",
                videoId = "vid_1",
                userId = "usr_blender_fan",
                userName = "Marcus Chen",
                userEmail = "marcus@example.com",
                userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop&q=80",
                content = "The 4K 60FPS render looks absolutely crystal clear on StreamVault! The animation detail on the fur is still unmatched.",
                likesCount = 42,
                isLikedByMe = false,
                timeAgo = "2 hours ago"
            ),
            CommentItem(
                id = "c_102",
                videoId = "vid_1",
                userId = "usr_animator",
                userName = "Elena Rostova",
                userEmail = "elena@example.com",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120&auto=format&fit=crop&q=80",
                content = "Classic Blender demo! Great to see offline multi-resolution download support as well.",
                likesCount = 18,
                isLikedByMe = false,
                timeAgo = "5 hours ago"
            ),
            CommentItem(
                id = "c_103",
                videoId = "vid_1",
                userId = "usr_tech",
                userName = "Liam Vance",
                userEmail = "liam@example.com",
                userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120&auto=format&fit=crop&q=80",
                content = "Audio-only mode and background playback works great for listening to the soundtrack!",
                likesCount = 9,
                isLikedByMe = false,
                timeAgo = "1 day ago"
            )
        )

        // Comments for Tears of Steel (vid_2)
        initialComments["vid_2"] = listOf(
            CommentItem(
                id = "c_201",
                videoId = "vid_2",
                userId = "usr_cyber",
                userName = "Kaito Tanaka",
                userEmail = "kaito@example.com",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop&q=80",
                content = "Amsterdam cyberpunk aesthetics never get old. The tracking and lighting integration is awesome.",
                likesCount = 31,
                isLikedByMe = false,
                timeAgo = "3 hours ago"
            ),
            CommentItem(
                id = "c_202",
                videoId = "vid_2",
                userId = "usr_vfx",
                userName = "Sarah Jenkins",
                userEmail = "sarah@example.com",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&auto=format&fit=crop&q=80",
                content = "Watched this in 1080p without any buffering. Smooth stream playback!",
                likesCount = 14,
                isLikedByMe = false,
                timeAgo = "6 hours ago"
            )
        )

        // Comments for Sintel (vid_3)
        initialComments["vid_3"] = listOf(
            CommentItem(
                id = "c_301",
                videoId = "vid_3",
                userId = "usr_fantasy",
                userName = "David Kim",
                userEmail = "david@example.com",
                userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120&auto=format&fit=crop&q=80",
                content = "That ending scene always hits right in the feelings. Beautiful art direction.",
                likesCount = 57,
                isLikedByMe = false,
                timeAgo = "1 day ago"
            )
        )

        _commentsMap.value = initialComments
    }

    fun getCommentsForVideo(videoId: String): Flow<List<CommentItem>> {
        return _commentsMap.map { map ->
            map[videoId] ?: emptyList()
        }
    }

    suspend fun postComment(
        videoId: String,
        user: UserSession,
        content: String
    ): Result<CommentItem> = withContext(dispatcher) {
        val newComment = CommentItem(
            id = "c_" + UUID.randomUUID().toString().take(8),
            videoId = videoId,
            userId = user.id,
            userName = user.displayName,
            userEmail = user.email,
            userAvatar = user.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop&q=80",
            content = content.trim(),
            likesCount = 0,
            isLikedByMe = false,
            timeAgo = "Just now"
        )

        // Optimistically update local state
        val currentList = _commentsMap.value[videoId] ?: emptyList()
        _commentsMap.value = _commentsMap.value + (videoId to (listOf(newComment) + currentList))

        // Attempt remote PostgREST sync if user has token and Supabase is configured
        try {
            if (user.accessToken != null && !user.isDemoAccount) {
                val dto = RemoteCommentDto(
                    videoId = videoId,
                    userId = user.id,
                    userEmail = user.email,
                    userName = user.displayName,
                    userAvatar = user.avatarUrl,
                    content = content.trim(),
                    likesCount = 0
                )
                SupabaseNetworkClient.restService.postComment(
                    authHeader = "Bearer ${user.accessToken}",
                    comment = dto
                )
            }
        } catch (e: Exception) {
            // Log or ignore network error since local update succeeded
        }

        Result.success(newComment)
    }

    fun toggleLikeComment(videoId: String, commentId: String) {
        val currentList = _commentsMap.value[videoId] ?: return
        val updated = currentList.map { comment ->
            if (comment.id == commentId) {
                val newLiked = !comment.isLikedByMe
                val newLikes = if (newLiked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                comment.copy(isLikedByMe = newLiked, likesCount = newLikes)
            } else {
                comment
            }
        }
        _commentsMap.value = _commentsMap.value + (videoId to updated)
    }
}
