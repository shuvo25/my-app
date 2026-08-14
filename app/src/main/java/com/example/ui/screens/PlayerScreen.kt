package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun PlayerScreen(
    viewModel: MediaViewModel,
    video: VideoItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val isBookmarked by viewModel.isCurrentVideoBookmarked.collectAsState()

    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val relatedVideos = remember(video.id) {
        viewModel.repository.getRelatedVideos(video.id)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Video Player View pinned at top
            VideoPlayerView(
                playerState = playerState,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onPositionUpdate = { pos, dur, buf -> viewModel.updatePlaybackPosition(pos, dur, buf) },
                onQualitySelect = { viewModel.selectQuality(it) },
                onSpeedSelect = { viewModel.setPlaybackSpeed(it) },
                onToggleFullscreen = { viewModel.toggleFullscreen() },
                onToggleAudioOnly = { viewModel.toggleAudioOnly() },
                onClose = onBack,
                onDownloadClick = { viewModel.openDownloadModal(video) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // Scrollable Content Below Player
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Title and Meta Info
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = video.formattedViews,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " • ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = video.uploadDate,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = video.category,
                                    color = AmberPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Interactive Action Bar (Like, Dislike, Download, Save, Share)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button
                        ActionPillButton(
                            icon = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            label = if (isLiked) "${(video.likes + 1) / 1000}K" else "${video.likes / 1000}K",
                            isActive = isLiked,
                            onClick = {
                                isLiked = !isLiked
                                if (isLiked) isDisliked = false
                            }
                        )

                        // Dislike Button
                        ActionPillButton(
                            icon = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                            label = "Dislike",
                            isActive = isDisliked,
                            onClick = {
                                isDisliked = !isDisliked
                                if (isDisliked) isLiked = false
                            }
                        )

                        // Download Button
                        ActionPillButton(
                            icon = Icons.Default.Download,
                            label = "Download",
                            isActive = true,
                            activeColor = AmberPrimary,
                            onClick = { viewModel.openDownloadModal(video) }
                        )

                        // Save / Bookmark
                        ActionPillButton(
                            icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            label = if (isBookmarked) "Saved" else "Save",
                            isActive = isBookmarked,
                            onClick = { viewModel.toggleBookmarkCurrentVideo() }
                        )

                        // Share Button
                        ActionPillButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            isActive = false,
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Watch '${video.title}' on StreamVault: ${video.defaultUrl}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                            }
                        )
                    }
                }

                // Channel Info Card
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(video.uploaderAvatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = video.uploaderName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = video.uploaderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${video.subscribersCount} subscribers",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { isSubscribed = !isSubscribed },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) MaterialTheme.colorScheme.surface else AmberPrimary,
                                    contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurface else Color.Black
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isSubscribed) "Subscribed" else "Subscribe",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Expandable Description & Tags
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = video.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    video.tags.take(3).forEach { tag ->
                                        Text(
                                            text = "#$tag",
                                            fontSize = 12.sp,
                                            color = CyanAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Text(
                                    text = if (isDescriptionExpanded) "Show less" else "Show more",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberPrimary
                                )
                            }
                        }
                    }
                }

                // Related Videos Header
                item {
                    Text(
                        text = "Related Videos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Related Videos Feed
                items(relatedVideos, key = { it.id }) { related ->
                    VideoCard(
                        video = related,
                        onClick = { viewModel.openVideoDetail(related) },
                        onDownloadClick = { viewModel.openDownloadModal(related) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPillButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = AmberPrimary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
