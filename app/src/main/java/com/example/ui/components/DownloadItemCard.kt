package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedLive

@Composable
fun DownloadItemCard(
    download: DownloadEntity,
    onPlayOffline: (DownloadEntity) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                if (download.status == DownloadStatus.COMPLETED) {
                    onPlayOffline(download)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Play overlay if completed
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(download.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = download.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (download.status == DownloadStatus.COMPLETED) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Offline",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Resolution Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = download.quality,
                        color = AmberPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details and progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (download.status == DownloadStatus.COMPLETED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = GreenSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Downloaded • ${download.formattedTotalSize}",
                            fontSize = 12.sp,
                            color = GreenSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Active progress bar
                    LinearProgressIndicator(
                        progress = { download.progress },
                        color = if (download.status == DownloadStatus.PAUSED) AmberPrimary.copy(alpha = 0.6f) else AmberPrimary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${download.progressPercent}% • ${download.formattedDownloadedSize} / ${download.formattedTotalSize}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (download.status == DownloadStatus.DOWNLOADING) {
                            Text(
                                text = "${download.formattedSpeed} • ${download.formattedEta}",
                                fontSize = 11.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (download.status == DownloadStatus.PAUSED) {
                            Text(
                                text = "Paused",
                                fontSize = 11.sp,
                                color = AmberPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons (Pause / Resume / Delete)
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(
                            onClick = { onPause(download.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = AmberPrimary
                            )
                        }
                    }
                    DownloadStatus.PAUSED -> {
                        IconButton(
                            onClick = { onResume(download.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = GreenSuccess
                            )
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        IconButton(
                            onClick = { onPlayOffline(download) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Play Offline",
                                tint = AmberPrimary
                            )
                        }
                    }
                    else -> {}
                }

                IconButton(
                    onClick = { onDelete(download.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = RedLive.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
