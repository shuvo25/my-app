package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VideoItem
import com.example.data.model.VideoQuality
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.PlayerState
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    playerState: PlayerState,
    onTogglePlay: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPositionUpdate: (Long, Long, Int) -> Unit,
    onQualitySelect: (VideoQuality) -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleAudioOnly: () -> Unit,
    onClose: () -> Unit,
    onDownloadClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val video = playerState.currentVideo ?: return
    val streamUrl = playerState.selectedQuality?.url ?: video.defaultUrl

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var currentDuration by remember { mutableLongStateOf(video.durationSeconds * 1000L) }
    var currentPos by remember { mutableLongStateOf(playerState.currentPositionMs) }
    var isBuffering by remember { mutableStateOf(false) }

    // Auto-hide controls timer
    LaunchedEffect(isControlsVisible, playerState.isPlaying) {
        if (isControlsVisible && playerState.isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Periodic position updater
    LaunchedEffect(playerState.isPlaying, videoViewRef) {
        while (true) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    val pos = vv.currentPosition.toLong()
                    val dur = vv.duration.toLong().let { if (it > 0) it else currentDuration }
                    val buf = vv.bufferPercentage
                    currentPos = pos
                    currentDuration = dur
                    onPositionUpdate(pos, dur, buf)
                }
            }
            delay(400)
        }
    }

    // Handle play / pause changes on VideoView
    LaunchedEffect(playerState.isPlaying) {
        videoViewRef?.let { vv ->
            if (playerState.isPlaying && !vv.isPlaying) {
                vv.start()
            } else if (!playerState.isPlaying && vv.isPlaying) {
                vv.pause()
            }
        }
    }

    // Handle quality stream URL change or initial seek
    LaunchedEffect(streamUrl) {
        videoViewRef?.let { vv ->
            isBuffering = true
            vv.setVideoURI(Uri.parse(streamUrl))
            vv.setOnPreparedListener { mp ->
                isBuffering = false
                if (playerState.currentPositionMs > 0) {
                    vv.seekTo(playerState.currentPositionMs.toInt())
                }
                if (playerState.playbackSpeed != 1.0f && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        mp.playbackParams = mp.playbackParams.setSpeed(playerState.playbackSpeed)
                    } catch (_: Exception) {}
                }
                if (playerState.isPlaying) {
                    vv.start()
                }
            }
            vv.setOnCompletionListener {
                onTogglePlay()
            }
            vv.setOnErrorListener { _, _, _ ->
                isBuffering = false
                true
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isControlsVisible = !isControlsVisible
                    },
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < width / 2) {
                            // Seek backwards 10s
                            val newPos = (currentPos - 10000).coerceAtLeast(0)
                            videoViewRef?.seekTo(newPos.toInt())
                            currentPos = newPos
                            onSeekTo(newPos)
                        } else {
                            // Seek forwards 10s
                            val newPos = (currentPos + 10000).coerceAtMost(currentDuration)
                            videoViewRef?.seekTo(newPos.toInt())
                            currentPos = newPos
                            onSeekTo(newPos)
                        }
                    }
                )
            }
    ) {
        if (!playerState.isAudioOnly) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setVideoURI(Uri.parse(streamUrl))
                        setOnPreparedListener { mp ->
                            isBuffering = false
                            if (playerState.currentPositionMs > 0) {
                                seekTo(playerState.currentPositionMs.toInt())
                            }
                            if (playerState.isPlaying) {
                                start()
                            }
                        }
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Audio Only Visualizer Mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color.Black)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Audio Only Mode",
                        tint = CyanAccent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Audio Stream Active",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Low Data Mode • Saving Bandwidth",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Buffering Indicator
        if (isBuffering) {
            CircularProgressIndicator(
                color = AmberPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quality button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showQualityDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playerState.selectedQuality?.resolution ?: "1080p",
                                    color = AmberPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Quality Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Speed button
                        IconButton(
                            onClick = { showSpeedDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(
                                text = "${playerState.playbackSpeed}x",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Audio Only toggle
                        IconButton(
                            onClick = onToggleAudioOnly,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isAudioOnly) Icons.Default.Videocam else Icons.Default.Headphones,
                                contentDescription = "Audio Toggle",
                                tint = if (playerState.isAudioOnly) CyanAccent else Color.White
                            )
                        }

                        // Direct Download Button
                        IconButton(
                            onClick = { onDownloadClick(video) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Video",
                                tint = AmberPrimary
                            )
                        }
                    }
                }

                // Center Play / Pause & Rewind Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (currentPos - 10000).coerceAtLeast(0)
                            videoViewRef?.seekTo(newPos.toInt())
                            currentPos = newPos
                            onSeekTo(newPos)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(64.dp)
                            .background(AmberPrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (currentPos + 10000).coerceAtMost(currentDuration)
                            videoViewRef?.seekTo(newPos.toInt())
                            currentPos = newPos
                            onSeekTo(newPos)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Seekbar & Timestamps
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPos),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTime(currentDuration),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (playerState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Slider(
                        value = if (currentDuration > 0) (currentPos.toFloat() / currentDuration.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { fraction ->
                            val targetMs = (fraction * currentDuration).toLong()
                            currentPos = targetMs
                        },
                        onValueChangeFinished = {
                            videoViewRef?.seekTo(currentPos.toInt())
                            onSeekTo(currentPos)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AmberPrimary,
                            activeTrackColor = AmberPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Quality Selection Modal
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = {
                Text(
                    text = "Streaming Quality",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    video.qualities.forEach { quality ->
                        val isSelected = playerState.selectedQuality?.resolution == quality.resolution
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onQualitySelect(quality)
                                    showQualityDialog = false
                                }
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
                                        onClick = {
                                            onQualitySelect(quality)
                                            showQualityDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = AmberPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = quality.resolution,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = quality.bitrate,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close", color = AmberPrimary)
                }
            }
        )
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text(
                    text = "Playback Speed",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    speeds.forEach { speed ->
                        val isSelected = playerState.playbackSpeed == speed
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSpeedSelect(speed)
                                    showSpeedDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AmberPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = AmberPrimary)
                }
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
