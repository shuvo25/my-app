package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.data.model.VideoQuality
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent

@Composable
fun DownloadQualityDialog(
    video: VideoItem,
    onConfirmDownload: (VideoItem, VideoQuality) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedQuality by remember { mutableStateOf(video.qualities.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select Video Resolution:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                video.qualities.forEach { quality ->
                    val isSelected = selectedQuality?.resolution == quality.resolution
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AmberPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedQuality = quality }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedQuality = quality },
                                    colors = RadioButtonDefaults.colors(selectedColor = AmberPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = quality.resolution,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) AmberPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = quality.bitrate,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = formatBytes(quality.sizeBytes),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedQuality?.let { q ->
                        onConfirmDownload(video, q)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Download", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
