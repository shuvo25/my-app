package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteCommentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "video_id") val videoId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "user_email") val userEmail: String,
    @Json(name = "user_name") val userName: String,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteBookmarkDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "video_id") val videoId: String,
    @Json(name = "title") val title: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String,
    @Json(name = "uploader_name") val uploaderName: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    @Json(name = "created_at") val createdAt: String? = null
)

data class CommentItem(
    val id: String,
    val videoId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userAvatar: String? = null,
    val content: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val timeAgo: String = "Just now"
)
