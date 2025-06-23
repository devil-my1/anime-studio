package com.sukuna.animestudio.presentation.detail

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sukuna.animestudio.R
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.presentation.components.LoadingIndicator
import kotlinx.coroutines.delay

/**
 * Main Anime Detail Screen that displays comprehensive anime information,
 * episode list, and integrated media player.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    onBack: () -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Hoisted Player State
    var showControls by remember { mutableStateOf(true) }
    var doubleTapSide by remember { mutableStateOf<DoubleTapSide?>(null) }
    var volume by remember { mutableFloatStateOf(1f) }

    val exoPlayer = remember(uiState.selectedEpisode?.videoUrl) {
        uiState.selectedEpisode?.videoUrl?.let {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(it.toUri()))
                prepare()
                addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        viewModel.seekTo(newPosition.positionMs)
                    }
                })
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    val progress = if ((uiState.selectedEpisode?.duration ?: 0) > 0) {
        (uiState.currentPosition / (uiState.selectedEpisode!!.duration * 60 * 1000f)).coerceIn(
            0f,
            1f
        )
    } else 0f

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(300), label = "controlsAlpha"
    )

    val doubleTapScale by animateFloatAsState(
        targetValue = if (doubleTapSide != null) 1.2f else 0f,
        animationSpec = tween(300), label = "doubleTapScale"
    )

    LaunchedEffect(doubleTapSide) {
        if (doubleTapSide != null) {
            delay(1000)
            doubleTapSide = null
        }
    }

    LaunchedEffect(showControls, uiState.isPlaying) {
        if (showControls && uiState.isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    if (uiState.isFullScreen && uiState.selectedEpisode != null) {
//        FullScreenPlayerContent(
//            episode = uiState.selectedEpisode!!,
//            exoPlayer = exoPlayer,
//            isPlaying = uiState.isPlaying,
//            currentPosition = uiState.currentPosition,
//            progress = progress,
//            showControls = showControls,
//            controlsAlpha = controlsAlpha,
//            doubleTapScale = doubleTapScale,
//            doubleTapSide = doubleTapSide,
//            volume = volume,
//            onPlayPause = { viewModel.togglePlayPause() },
//            onSeek = { viewModel.seekTo(it) },
//            onFullScreenToggle = { viewModel.toggleFullScreen() },
//            onShowControls = { showControls = it },
//            onDoubleTapSide = { doubleTapSide = it },
//            onVolumeChange = { volume = it }
//        )
        FullScreenPlayer(
            episode = uiState.selectedEpisode!!,
            onToggleFullScreen = { viewModel.toggleFullScreen() }
        )
    } else {
        Scaffold(
            topBar = {
                if (!uiState.isFullScreen) {
                    TopAppBar(
                        title = {
                            Text(
                                text = uiState.anime?.title ?: "Anime Details",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            if (currentUser != null && uiState.anime != null) {
                                UserStatusChip(
                                    status = getUserAnimeStatus(uiState.anime, currentUser),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (currentUser?.favoriteAnime?.any { it.id == uiState.anime?.id } == true) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = "Toggle Favorite",
                                    tint = if (currentUser?.favoriteAnime?.any { it.id == uiState.anime?.id } == true) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.error_icon),
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Unknown error occurred",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadAnimeDetails() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.anime != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            if (!uiState.isFullScreen) {
                                AnimeInfoSection(
                                    anime = uiState.anime!!,
                                    userStatus = getUserAnimeStatus(uiState.anime, currentUser),
                                    onAddToWatchlist = { viewModel.addToWatchlist() },
                                    onDropAnime = { viewModel.dropAnime() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        item {
                            if (uiState.selectedEpisode != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
//                                    FancyPlayerContent(
//                                        episode = uiState.selectedEpisode!!,
//                                        exoPlayer = exoPlayer,
//                                        isPlaying = uiState.isPlaying,
//                                        currentPosition = uiState.currentPosition,
//                                        progress = progress,
//                                        showControls = showControls,
//                                        controlsAlpha = controlsAlpha,
//                                        doubleTapScale = doubleTapScale,
//                                        doubleTapSide = doubleTapSide,
//                                        volume = volume,
//                                        onPlayPause = { viewModel.togglePlayPause() },
//                                        onSeek = { viewModel.seekTo(it) },
//                                        onFullScreenToggle = { viewModel.toggleFullScreen() },
//                                        onShowControls = { showControls = it },
//                                        onDoubleTapSide = { doubleTapSide = it },
//                                        onVolumeChange = { volume = it },
//                                        isFullScreen = false
//                                    )
                                    Media3PlayerSection(
                                        episode = uiState.selectedEpisode!!,
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        onToggleFullScreen = { viewModel.toggleFullScreen() })
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        if (!uiState.isFullScreen) {
                            item {
                                Text(
                                    text = "Episodes (${uiState.anime!!.episodes.size})",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            items(
                                items = uiState.anime!!.episodes.sortedBy { it.episodeNumber },
                                key = { it.id }
                            ) { episode ->
                                EpisodeItem(
                                    episode = episode,
                                    isSelected = uiState.selectedEpisode?.id == episode.id,
                                    onEpisodeClick = { viewModel.selectEpisode(episode) },
                                    onMarkWatched = { viewModel.markEpisodeAsWatched(episode) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
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
private fun getUserAnimeStatus(
    anime: Anime?,
    user: User?
): String {
    if (anime == null || user == null) return "Not in List"
    return when {
        user.watchingAnime.any { it.id == anime.id } -> "Watching"
        user.completedAnime.any { it.id == anime.id } -> "Completed"
        user.watchlist.any { it.id == anime.id } -> "Plan to Watch"
        user.droppedAnime.any { it.id == anime.id } -> "Dropped"
        else -> "Not in List"
    }
}

/**
 * Displays a user status chip showing the user's relationship with the anime.
 */
@Composable
private fun UserStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        "Watching" -> Pair(Color(0xFF4CAF50), Color.White)
        "Completed" -> Pair(Color(0xFF2196F3), Color.White)
        "Plan to Watch" -> Pair(Color(0xFFFF9800), Color.White)
        "Dropped" -> Pair(Color(0xFFF44336), Color.White)
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Displays comprehensive anime information including cover image, title,
 * description, genres, rating, and status.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun AnimeInfoSection(
    anime: Anime,
    userStatus: String,
    onAddToWatchlist: () -> Unit,
    onDropAnime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Hero Section with Cover Image and Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Cover Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Anime Cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Anime Info Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating and Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = String.format("%.1f", anime.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    // Status
                    StatusChip(status = anime.animeStatus)
                }
            }
        }

        // User Action Buttons
        if (userStatus != "Watching") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddToWatchlist,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Watching")
                }

                if (userStatus == "Watching") {
                    OutlinedButton(
                        onClick = onDropAnime,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Drop")
                    }
                }
            }
        }

        // Detailed Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Genres
            if (anime.genre.isNotEmpty()) {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(anime.genre.take(5)) { genre ->
                        GenreChip(genre = genre)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Release Date
            if (anime.releaseDate.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Release Date",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Released: ${anime.releaseDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Episodes Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Episodes",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${anime.episodesCount} Episodes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            if (anime.description.isNotEmpty()) {
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = anime.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

/**
 * Displays a status chip with appropriate color coding.
 */
@Composable
private fun StatusChip(status: com.sukuna.animestudio.domain.model.AnimeStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        com.sukuna.animestudio.domain.model.AnimeStatus.IN_PROGRESS ->
            Triple(Color(0xFF4CAF50), Color.White, "Ongoing")

        com.sukuna.animestudio.domain.model.AnimeStatus.COMPLETED ->
            Triple(Color(0xFF2196F3), Color.White, "Completed")

        com.sukuna.animestudio.domain.model.AnimeStatus.SOON_ARRIVING ->
            Triple(Color(0xFFFF9800), Color.White, "Coming Soon")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Displays a genre chip with consistent styling.
 */
@Composable
private fun GenreChip(genre: com.sukuna.animestudio.domain.model.AnimeGenre) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = genre.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
} 