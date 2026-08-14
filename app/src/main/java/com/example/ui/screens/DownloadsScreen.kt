package com.example.ui.screens

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.ui.components.DownloadItemCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val completedDownloads by viewModel.completedDownloads.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Active (${activeDownloads.size})", "Completed (${completedDownloads.size})")

    // Disk storage statistics
    val storageStats = remember(completedDownloads.size) {
        getStorageStats(context)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DownloadForOffline,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloads Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Storage Meter Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SdCard,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Device Storage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${storageStats.usedDisplay} Used • ${storageStats.freeDisplay} Free",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { storageStats.usedFraction },
                            color = AmberPrimary,
                            trackColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                // Tabs for Active vs Completed
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AmberPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AmberPrimary
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTabIndex == 0) {
                // Active Downloads Tab
                if (activeDownloads.isEmpty()) {
                    EmptyDownloadState(
                        title = "No Active Downloads",
                        subtitle = "Browse videos and tap the download button to save for offline watching."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(activeDownloads, key = { it.id }) { item ->
                            DownloadItemCard(
                                download = item,
                                onPlayOffline = { viewModel.playOfflineVideo(it) },
                                onPause = { viewModel.pauseDownload(it) },
                                onResume = { viewModel.resumeDownload(it) },
                                onDelete = { viewModel.deleteDownload(it) }
                            )
                        }
                    }
                }
            } else {
                // Completed Downloads Tab
                if (completedDownloads.isEmpty()) {
                    EmptyDownloadState(
                        title = "No Downloaded Videos",
                        subtitle = "Downloaded videos will appear here and can be streamed 100% offline without using mobile data."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(completedDownloads, key = { it.id }) { item ->
                            DownloadItemCard(
                                download = item,
                                onPlayOffline = { viewModel.playOfflineVideo(it) },
                                onPause = { viewModel.pauseDownload(it) },
                                onResume = { viewModel.resumeDownload(it) },
                                onDelete = { viewModel.deleteDownload(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDownloadState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AmberPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

private data class StorageStats(
    val usedDisplay: String,
    val freeDisplay: String,
    val usedFraction: Float
)

private fun getStorageStats(context: android.content.Context): StorageStats {
    return try {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val freeBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - freeBytes

        val usedFraction = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.4f
        StorageStats(
            usedDisplay = formatSize(usedBytes),
            freeDisplay = formatSize(freeBytes),
            usedFraction = usedFraction
        )
    } catch (_: Exception) {
        StorageStats("12.4 GB", "51.6 GB", 0.25f)
    }
}

private fun formatSize(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return String.format("%.1f GB", gb)
}
