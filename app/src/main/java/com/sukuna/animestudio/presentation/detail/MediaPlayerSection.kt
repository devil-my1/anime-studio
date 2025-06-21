package com.sukuna.animestudio.presentation.detail

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Episode
import kotlin.math.roundToInt

/**
 * Fancy and intuitive media player component with comprehensive controls.
 * Features play/pause, seek functionality, progress tracking, and full-screen toggle.
 */
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Video Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onPlayPause() }
            ) {
                // Episode Thumbnail
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
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }
                }

                // Episode Info Overlay
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
            }

            // Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Progress Bar
                ProgressBar(
                    progress = animatedProgress,
                    onSeek = { seekPosition ->
                        val newPosition =
                            (seekPosition * episode.duration * 60 * 1000).roundToInt().toLong()
                        onSeek(newPosition)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Control Buttons and Time Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Display
                    Text(
                        text = formatTime(currentPosition, episode.duration * 60 * 1000L),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Control Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Episode Button
                        IconButton(
                            onClick = { /* TODO: Navigate to previous episode */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.next),
                                contentDescription = "Previous Episode",
                                modifier = Modifier.rotate(180f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Play/Pause Button
                        FloatingActionButton(
                            onClick = onPlayPause,
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                painter = if (isPlaying) painterResource(R.drawable.pause_button_icon) else painterResource(
                                    R.drawable.play_button_icon
                                ),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Next Episode Button
                        IconButton(
                            onClick = { /* TODO: Navigate to next episode */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.next),
                                contentDescription = "Next Episode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Volume and Settings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* TODO: Volume control */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.sound),
                                contentDescription = "Volume",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* TODO: Settings menu */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom progress bar with seek functionality.
 */
@Composable
private fun ProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Progress Bar Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(2.dp)
                )
                .clickable {
                    // Calculate seek position based on click offset
                    // This is a simplified implementation
                    onSeek(progress)
                }
        ) {
            // Progress Bar Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(2.dp)
                    )
            )

            // Progress Thumb
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (progress * 100).coerceIn(0f, 100f).dp)
                    .size(16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

/**
 * Formats time in MM:SS format for display.
 */
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