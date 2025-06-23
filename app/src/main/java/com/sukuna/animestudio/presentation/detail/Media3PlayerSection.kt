package com.sukuna.animestudio.presentation.detail

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.sukuna.animestudio.domain.model.Episode

/**
 * A reliable video player using Google's Media3 (ExoPlayer) library.
 * This is the recommended approach for video playback on Android.
 *
 * @param episode The episode containing the video URL to play.
 * @param onToggleFullScreen Callback to toggle full-screen mode.
 * @param modifier Modifier for this composable.
 */
@Composable
fun Media3PlayerSection(
    episode: Episode,
    onToggleFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUrl = episode.videoUrl

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val playerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = true
            setFullscreenButtonClickListener {
                onToggleFullScreen()
            }
        }
    }

    AndroidView(
        factory = { playerView },
        modifier = modifier
    )
}

@Composable
fun FullScreenPlayer(
    episode: Episode,
    onToggleFullScreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Media3PlayerSection(
            episode = episode,
            onToggleFullScreen = onToggleFullScreen,
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (activity != null) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
} 