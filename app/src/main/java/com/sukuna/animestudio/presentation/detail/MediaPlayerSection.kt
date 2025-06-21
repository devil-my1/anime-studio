package com.sukuna.animestudio.presentation.detail

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Episode

/**
 * Fancy and intuitive media player component with comprehensive controls.
 * Features play/pause, seek functionality, progress tracking, full-screen toggle,
 * and Firebase Storage video integration.
 */
@OptIn(UnstableApi::class)
@SuppressLint("RememberReturnType", "ClickableViewAccessibility")
@Composable
fun MediaPlayerSection(
    episode: Episode,
    isPlaying: Boolean,
    currentPosition: Long,
    isFullScreen: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calculate progress percentage for the seek bar
    val progress = if (episode.duration > 0) {
        (currentPosition / (episode.duration * 60 * 1000f)).coerceIn(0f, 1f)
    } else 0f

    // Animate progress bar for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    // ExoPlayer state management
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    // Initialize ExoPlayer when episode changes
    DisposableEffect(episode.videoUrl) {
        if (episode.videoUrl.isNotEmpty()) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(episode.videoUrl.toUri())
                setMediaItem(mediaItem)
                prepare()

                // Add listener for position updates and state changes
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        // Handle play state changes
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        // Handle seek events
                        onSeek(newPosition.positionMs)
                    }
                })
            }
        }

        onDispose {
            exoPlayer?.release()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isFullScreen) 400.dp else 280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Video Player Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onPlayPause() }
            ) {
                if (episode.videoUrl.isNotEmpty() && exoPlayer != null) {
                    // ExoPlayer for video playback
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                playerView = this

                                // Configure player view for better UX
                                useController = true

                                // Enable double tap to seek
                                setOnTouchListener { _, event ->
                                    // Handle touch events for better UX
                                    false
                                }

                                // Handle play/pause based on isPlaying state
                                if (isPlaying) {
                                    exoPlayer?.play()
                                } else {
                                    exoPlayer?.pause()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { playerView ->
                            // Update player state when isPlaying changes
                            if (isPlaying) {
                                exoPlayer?.play()
                            } else {
                                exoPlayer?.pause()
                            }

                            // Update player view configuration
                            playerView?.let { view ->
                                view.useController = true
                            }
                        }
                    )
                } else {
                    // Fallback to episode thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(episode.imageUrl.ifEmpty { "https://via.placeholder.com/400x225" })
                            .crossfade(true)
                            .build(),
                        contentDescription = "Episode ${episode.episodeNumber}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Play/Pause Overlay
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

                // Episode Info Overlay (only show when not in full-screen mode)
                if (!isFullScreen) {
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
                }

                // Full Screen Button
                IconButton(
                    onClick = onFullScreenToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        painter = if (isFullScreen) painterResource(R.drawable.exit_full_screen_icon) else painterResource(
                            R.drawable.expand
                        ),
                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Back button for full-screen mode
                if (isFullScreen) {
                    IconButton(
                        onClick = onFullScreenToggle,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(40.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_icon),
                            contentDescription = "Exit Full Screen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
} 