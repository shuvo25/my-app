package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.VideoItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.VideoCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onDownloadClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videoList.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var isGridView by remember { mutableStateOf(false) }

    val featuredVideo = videos.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "StreamVault",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Grid / List toggle
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List View",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Import stream button
                    IconButton(onClick = { viewModel.showAddStreamDialog() }) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "Add Stream URL",
                            tint = AmberPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddStreamDialog() },
                containerColor = AmberPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import URL", fontWeight = FontWeight.Bold)
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
            // Category filter chips
            CategoryChipsRow(
                categories = viewModel.repository.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onDownloadClick = { onDownloadClick(video) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Featured Hero Banner (Top of Home feed)
                    if (selectedCategory == "All" && featuredVideo != null) {
                        item(key = "hero_banner") {
                            FeaturedHeroBanner(
                                video = featuredVideo,
                                onWatchClick = { onVideoClick(featuredVideo) },
                                onDownloadClick = { onDownloadClick(featuredVideo) }
                            )
                        }
                    }

                    items(videos, key = { it.id }) { video ->
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

@Composable
private fun FeaturedHeroBanner(
    video: VideoItem,
    onWatchClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onWatchClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_stream),
                contentDescription = "Featured Stream Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberPrimary,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FEATURED 4K STREAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                // Bottom Title & CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${video.uploaderName} • ${video.formattedViews}",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onWatchClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stream", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
