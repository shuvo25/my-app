package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.BookmarkEntity
import com.example.data.model.VideoItem
import com.example.data.model.WatchHistoryEntity
import com.example.ui.components.VideoCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: MediaViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onDownloadClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val history by viewModel.watchHistory.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("History (${history.size})", "Bookmarks (${bookmarks.size})")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Library & Saved",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        if (selectedTabIndex == 0 && history.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearWatchHistory() }) {
                                Text("Clear History", color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

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
                    tabs.forEachIndexed { index, title ->
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
                // Watch History with resume position
                if (history.isEmpty()) {
                    EmptyLibraryState(
                        title = "No Watch History",
                        subtitle = "Videos you watch will be saved here so you can pick up right where you left off."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(history, key = { it.videoId }) { item ->
                            val video = viewModel.repository.getVideoById(item.videoId)
                            if (video != null) {
                                HistoryItemCard(
                                    history = item,
                                    onClick = { onVideoClick(video) },
                                    onDownloadClick = { onDownloadClick(video) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Bookmarks
                if (bookmarks.isEmpty()) {
                    EmptyLibraryState(
                        title = "No Bookmarks",
                        subtitle = "Tap the Save button on any video to create your personalized collection."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bookmarks, key = { it.videoId }) { item ->
                            val video = viewModel.repository.getVideoById(item.videoId)
                            if (video != null) {
                                VideoCard(
                                    video = video,
                                    onClick = { onVideoClick(video) },
                                    onDownloadClick = { onDownloadClick(video) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    history: WatchHistoryEntity,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(history.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = history.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Progress Bar overlay
                LinearProgressIndicator(
                    progress = { history.progressPercentage },
                    color = AmberPrimary,
                    trackColor = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${history.uploaderName} • Resume at ${(history.progressPercentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = AmberPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = AmberPrimary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
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
