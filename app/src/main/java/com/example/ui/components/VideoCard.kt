package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Top-Left Resolution Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.qualities.firstOrNull()?.resolution ?: "HD",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Category Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberPrimary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.category,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Bottom-Right Duration Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Channel Avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.uploaderAvatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.uploaderName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Title and details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = video.uploaderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = video.formattedViews,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = video.uploadDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Download action button
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Video",
                        tint = AmberPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
