package com.sukuna.animestudio.presentation.detail

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Ultra-fancy and beautiful media player with custom Compose overlay.
 * Features stunning animations, gesture controls, glassmorphism effects,
 * and perfect full-screen experience.
 */
@OptIn(UnstableApi::class)
@SuppressLint("RememberReturnType", "ClickableViewAccessibility", "ContextCastToActivity")
@Composable
fun MediaPlayerSection(
    episode: Episode,
    isPlaying: Boolean,
    currentPosition: Long,
    isFullScreen: Boolean,
    showControls: Boolean,
    doubleTapSide: DoubleTapSide?,
    volume: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    onShowControlsChange: (Boolean) -> Unit,
    onDoubleTapSideChange: (DoubleTapSide?) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(doubleTapSide) {
        if (doubleTapSide != null) {
            delay(1000)
            onDoubleTapSideChange(null)
        }
    }

    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(3000)
            onShowControlsChange(false)
        }
    }

    val progress = if (episode.duration > 0) {
        (currentPosition / (episode.duration * 60 * 1000f)).coerceIn(0f, 1f)
    } else 0f

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(300),
        label = "controls"
    )

    val doubleTapScale by animateFloatAsState(
        targetValue = if (doubleTapSide != null) 1.2f else 0f,
        animationSpec = tween(300),
        label = "doubleTap"
    )

    DisposableEffect(episode.videoUrl) {
        if (episode.videoUrl.isNotEmpty()) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(episode.videoUrl.toUri())
                setMediaItem(mediaItem)
                prepare()
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {}
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        onSeek(newPosition.positionMs)
                    }
                })
            }
        }
        onDispose {
            exoPlayer?.release()
        }
    }

    if (isFullScreen) {
        FullScreenPlayerContent(
            episode = episode,
            exoPlayer = exoPlayer,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            progress = progress,
            showControls = showControls,
            controlsAlpha = controlsAlpha,
            doubleTapScale = doubleTapScale,
            doubleTapSide = doubleTapSide,
            volume = volume,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onFullScreenToggle = onFullScreenToggle,
            onShowControls = onShowControlsChange,
            onDoubleTapSide = onDoubleTapSideChange,
            onVolumeChange = onVolumeChange,
        )
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            FancyPlayerContent(
                episode = episode,
                exoPlayer = exoPlayer,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                progress = progress,
                showControls = showControls,
                controlsAlpha = controlsAlpha,
                doubleTapScale = doubleTapScale,
                doubleTapSide = doubleTapSide,
                volume = volume,
                onPlayPause = onPlayPause,
                onSeek = onSeek,
                onFullScreenToggle = onFullScreenToggle,
                onShowControls = onShowControlsChange,
                onDoubleTapSide = onDoubleTapSideChange,
                onVolumeChange = onVolumeChange,
                isFullScreen = false
            )
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
internal fun FullScreenPlayerContent(
    episode: Episode,
    exoPlayer: ExoPlayer?,
    isPlaying: Boolean,
    currentPosition: Long,
    progress: Float,
    showControls: Boolean,
    controlsAlpha: Float,
    doubleTapScale: Float,
    doubleTapSide: DoubleTapSide?,
    volume: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    onShowControls: (Boolean) -> Unit,
    onDoubleTapSide: (DoubleTapSide?) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            onDispose {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            onDispose {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onShowControls(!showControls)
                    },
                    onDoubleTap = { offset ->
                        val side =
                            if (offset.x < size.width / 2) DoubleTapSide.LEFT else DoubleTapSide.RIGHT
                        onDoubleTapSide(side)
                        val seekAmount = if (side == DoubleTapSide.RIGHT) 10000L else -10000L
                        val newPosition = (currentPosition + seekAmount).coerceAtLeast(0L)
                        onSeek(newPosition)
                        exoPlayer?.seekTo(newPosition)
                    }
                )
            }
    ) {
        if (episode.videoUrl.isNotEmpty() && exoPlayer != null) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = false
                        setOnTouchListener { _, _ -> true }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    if (isPlaying) exoPlayer.play() else exoPlayer.pause()
                }
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(episode.imageUrl.ifEmpty { "https://via.placeholder.com/1920x1080" })
                    .crossfade(true)
                    .build(),
                contentDescription = "Episode ${episode.episodeNumber}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        if (doubleTapSide != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = if (doubleTapSide == DoubleTapSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    painter = if (doubleTapSide == DoubleTapSide.LEFT) painterResource(R.drawable.rewind_icon) else painterResource(
                        R.drawable.forward_icon
                    ),
                    contentDescription = "Seek",
                    modifier = Modifier
                        .size(80.dp)
                        .scale(doubleTapScale)
                        .alpha(doubleTapScale),
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(controlsAlpha)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FancyIconButton(
                    onClick = onFullScreenToggle,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Full Screen"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Episode ${episode.episodeNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (episode.title.isNotEmpty()) {
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                FancyIconButton(
                    onClick = { /* onShowVolumeControl(!showVolumeControl) */ },
                    icon = painterResource(R.drawable.sound),
                    contentDescription = "Volume"
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                FancyPlayButton(
                    isPlaying = isPlaying,
                    onClick = onPlayPause,
                    modifier = Modifier.size(80.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                FancyProgressBar(
                    progress = progress,
                    onSeek = { seekPosition ->
                        val newPosition =
                            (seekPosition * episode.duration * 60 * 1000).roundToInt().toLong()
                        onSeek(newPosition)
                        exoPlayer?.seekTo(newPosition)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition, episode.duration * 60 * 1000L),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FancyIconButton(
                            onClick = { /* Previous episode */ },
                            icon = painterResource(R.drawable.prev),
                            contentDescription = "Previous Episode"
                        )
                        FancyIconButton(
                            onClick = onPlayPause,
                            icon = if (isPlaying) painterResource(R.drawable.pause_button_icon) else painterResource(
                                R.drawable.play_button_icon
                            ),
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                        FancyIconButton(
                            onClick = { /* Next episode */ },
                            icon = painterResource(R.drawable.next),
                            contentDescription = "Next Episode"
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FancyPlayerContent(
    episode: Episode,
    exoPlayer: ExoPlayer?,
    isPlaying: Boolean,
    currentPosition: Long,
    progress: Float,
    showControls: Boolean,
    controlsAlpha: Float,
    doubleTapScale: Float,
    doubleTapSide: DoubleTapSide?,
    volume: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    onShowControls: (Boolean) -> Unit,
    onDoubleTapSide: (DoubleTapSide?) -> Unit,
    onVolumeChange: (Float) -> Unit,
    isFullScreen: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onShowControls(!showControls)
                        },
                        onDoubleTap = { offset ->
                            val side =
                                if (offset.x < size.width / 2) DoubleTapSide.LEFT else DoubleTapSide.RIGHT
                            onDoubleTapSide(side)
                            val seekAmount = if (side == DoubleTapSide.RIGHT) 10000L else -10000L
                            val newPosition = (currentPosition + seekAmount).coerceAtLeast(0L)
                            onSeek(newPosition)
                            exoPlayer?.seekTo(newPosition)
                        }
                    )
                }
        ) {
            if (episode.videoUrl.isNotEmpty() && exoPlayer != null) {
                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            player = exoPlayer
                            useController = false
                            setOnTouchListener { _, _ -> true }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { if (isPlaying) exoPlayer?.play() else exoPlayer?.pause() }
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(episode.imageUrl.ifEmpty { "https://via.placeholder.com/400x225" })
                        .crossfade(true)
                        .build(),
                    contentDescription = "Episode ${episode.episodeNumber}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isPlaying) 0.3f else 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isPlaying) {
                        Icon(
                            painter = painterResource(R.drawable.play_button_icon),
                            contentDescription = "Play",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            if (doubleTapSide != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = if (doubleTapSide == DoubleTapSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        painter = if (doubleTapSide == DoubleTapSide.LEFT) painterResource(R.drawable.rewind_icon) else painterResource(
                            R.drawable.forward_icon
                        ),
                        contentDescription = "Seek",
                        modifier = Modifier
                            .size(60.dp)
                            .scale(doubleTapScale)
                            .alpha(doubleTapScale),
                        tint = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(controlsAlpha)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Episode ${episode.episodeNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (episode.title.isNotEmpty()) {
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                FancyIconButton(
                    onClick = onFullScreenToggle,
                    icon = if (isFullScreen) painterResource(R.drawable.exit_full_screen_icon) else painterResource(
                        R.drawable.expand
                    ),
                    contentDescription = "Full Screen",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    FancyPlayButton(
                        isPlaying = isPlaying,
                        onClick = onPlayPause,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FancyIconButton(
    onClick: () -> Unit,
    icon: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(
                Color.Black.copy(alpha = 0.6f),
                CircleShape
            )
    ) {
        when (icon) {
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            is Painter -> {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            else -> throw IllegalArgumentException("Unsupported icon type")
        }
    }
}

@Composable
private fun FancyPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 0.8f else 1f,
        animationSpec = tween(200),
        label = "playScale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        shape = CircleShape
    ) {
        Icon(
            painter = if (isPlaying) painterResource(R.drawable.pause_button_icon) else painterResource(
                R.drawable.play_button_icon
            ),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun FancyProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(progress) {
        if (!isDragging) {
            dragProgress = progress
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek(dragProgress)
                        },
                        onDrag = { _, dragAmount ->
                            val newProgress =
                                (dragProgress + dragAmount.x / size.width).coerceIn(0f, 1f)
                            dragProgress = newProgress
                        }
                    )
                }
                .clickable {
                    onSeek(progress)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isDragging) dragProgress else progress)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(2.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(
                        x = ((if (isDragging) dragProgress else progress) * 100).coerceIn(
                            0f,
                            100f
                        ).dp
                    )
                    .size(16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun FancyVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = when {
                    volume == 0f -> painterResource(R.drawable.volume_off_icon)
                    volume < 0.5f -> painterResource(R.drawable.sound)
                    else -> painterResource(R.drawable.sound)
                },
                contentDescription = "Volume",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(volume)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(2.dp)
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(currentPosition: Long, totalDuration: Long): String {
    val currentMinutes = (currentPosition / 60000).toInt()
    val currentSeconds = ((currentPosition % 60000) / 1000).toInt()

    val totalMinutes = (totalDuration / 60000).toInt()
    val totalSeconds = ((totalDuration % 60000) / 1000).toInt()

    return String.format(
        "%02d:%02d / %02d:%02d",
        currentMinutes,
        currentSeconds,
        totalMinutes,
        totalSeconds
    )
}

enum class DoubleTapSide {
    LEFT, RIGHT
} 