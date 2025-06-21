# Firebase Storage Integration for Anime Episodes

## Overview

This implementation provides a complete Firebase Storage integration for dynamically fetching and playing anime episodes in your Android app. The system automatically discovers episodes in Firebase Storage and integrates them with your existing media player.

## Firebase Storage Structure

```
gs://animestudioapp.firebasestorage.app/
└── animes_storage/
    ├── attack_on_titan/
    │   ├── episode_1.mp4
    │   ├── episode_2.mp4
    │   └── episode_3.mp4
    ├── naruto/
    │   ├── episode_1.mp4
    │   ├── episode_2.mp4
    │   └── ...
    └── one_piece/
        ├── episode_1.mp4
        ├── episode_2.mp4
        └── ...
```

## Key Components

### 1. StorageRepository Interface

```kotlin
interface StorageRepository {
    /**
     * Fetches all available episodes for a specific anime from Firebase Storage.
     * @param animeId The ID or title of the anime
     * @return List of episodes with their Firebase Storage URLs
     */
    suspend fun getAnimeEpisodes(animeId: String): Result<List<Episode>>
    
    /**
     * Fetches a specific episode's video URL from Firebase Storage.
     * @param animeId The ID or title of the anime
     * @param episodeNumber The episode number to fetch
     * @return The video URL for the episode
     */
    suspend fun getEpisodeVideoUrl(animeId: String, episodeNumber: Int): Result<String>
    
    /**
     * Lists all available anime folders in Firebase Storage.
     * @return List of anime IDs/titles available in storage
     */
    suspend fun getAvailableAnimeList(): Result<List<String>>
    
    /**
     * Checks if a specific episode exists in Firebase Storage.
     * @param animeId The ID or title of the anime
     * @param episodeNumber The episode number to check
     * @return True if the episode exists, false otherwise
     */
    suspend fun episodeExists(animeId: String, episodeNumber: Int): Result<Boolean>
}
```

### 2. StorageRepositoryImpl Implementation

```kotlin
@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {
    
    private val ANIMES_STORAGE_PATH = "animes_storage"
    
    override suspend fun getAnimeEpisodes(animeId: String): Result<List<Episode>> {
        return try {
            // Get reference to the anime folder
            val animeFolderRef = storage.reference
                .child(ANIMES_STORAGE_PATH)
                .child(animeId)
            
            // List all files in the anime folder
            val result = animeFolderRef.listAll().await()
            
            val episodes = mutableListOf<Episode>()
            
            // Process each file in the folder
            result.items.forEach { fileRef ->
                val fileName = fileRef.name
                
                // Check if the file is a video file (episode)
                if (isVideoFile(fileName)) {
                    val episodeNumber = extractEpisodeNumber(fileName)
                    if (episodeNumber != null) {
                        // Get the download URL for the video
                        val downloadUrl = fileRef.downloadUrl.await()
                        
                        val episode = Episode(
                            id = "${animeId}_episode_$episodeNumber",
                            title = "Episode $episodeNumber",
                            episodeNumber = episodeNumber,
                            animeId = animeId,
                            videoUrl = downloadUrl.toString(),
                            duration = 24 // Default duration
                        )
                        
                        episodes.add(episode)
                    }
                }
            }
            
            // Sort episodes by episode number
            val sortedEpisodes = episodes.sortedBy { it.episodeNumber }
            Result.success(sortedEpisodes)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper functions for file processing
    private fun isVideoFile(fileName: String): Boolean {
        val videoExtensions = listOf(".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm")
        return videoExtensions.any { fileName.lowercase().endsWith(it) }
    }
    
    private fun extractEpisodeNumber(fileName: String): Int? {
        val patterns = listOf(
            Regex("episode_(\\d+)"),
            Regex("ep(\\d+)"),
            Regex("(\\d+)"),
            Regex("e(\\d+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(fileName.lowercase())
            if (match != null) {
                return match.groupValues[1].toInt()
            }
        }
        return null
    }
}
```

### 3. Updated Episode Model

```kotlin
data class Episode(
    val id: String = Random.nextInt().toString(),
    val title: String = "",
    val description: String = "",
    val episodeNumber: Int = 1,
    val animeId: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "", // Firebase Storage URL for video playback
    val duration: Int = 24, // in minutes
    val isWatched: Boolean = false,
)
```

### 4. AnimeDetailViewModel Integration

```kotlin
@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val dbRepository: DbRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val storageRepository: StorageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private fun loadAnimeDetailsInternal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // First, load anime details from database
                val anime = dbRepository.getAnimeById(animeId)
                if (anime != null) {
                    // Try to fetch episodes from Firebase Storage
                    val storageEpisodesResult = storageRepository.getAnimeEpisodes(animeId)
                    
                    val updatedAnime = if (storageEpisodesResult.isSuccess) {
                        val storageEpisodes = storageEpisodesResult.getOrNull() ?: emptyList()
                        
                        // Merge database episodes with storage episodes
                        val mergedEpisodes = mergeEpisodes(anime.episodes, storageEpisodes)
                        
                        anime.copy(episodes = mergedEpisodes)
                    } else {
                        // If Firebase Storage fails, use database episodes
                        anime
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            anime = updatedAnime,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load anime details"
                    )
                }
            }
        }
    }
    
    /**
     * Merges database episodes with Firebase Storage episodes.
     * Prioritizes storage episodes for video URLs while preserving database metadata.
     */
    private fun mergeEpisodes(dbEpisodes: List<Episode>, storageEpisodes: List<Episode>): List<Episode> {
        val mergedEpisodes = mutableListOf<Episode>()
        
        // Create a map of storage episodes by episode number for quick lookup
        val storageEpisodesMap = storageEpisodes.associateBy { it.episodeNumber }
        
        // Process database episodes and merge with storage data
        dbEpisodes.forEach { dbEpisode ->
            val storageEpisode = storageEpisodesMap[dbEpisode.episodeNumber]
            
            val mergedEpisode = if (storageEpisode != null) {
                // Merge database metadata with storage video URL
                dbEpisode.copy(videoUrl = storageEpisode.videoUrl)
            } else {
                // Keep database episode as is
                dbEpisode
            }
            
            mergedEpisodes.add(mergedEpisode)
        }
        
        // Add any storage episodes that don't exist in database
        storageEpisodes.forEach { storageEpisode ->
            val existsInDb = dbEpisodes.any { it.episodeNumber == storageEpisode.episodeNumber }
            if (!existsInDb) {
                mergedEpisodes.add(storageEpisode)
            }
        }
        
        // Sort by episode number
        return mergedEpisodes.sortedBy { it.episodeNumber }
    }
}
```

### 5. Media Player with ExoPlayer Integration

```kotlin
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
    
    // ExoPlayer state management
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    
    // Initialize ExoPlayer when episode changes
    remember(episode.videoUrl) {
        if (episode.videoUrl.isNotEmpty()) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(episode.videoUrl))
                setMediaItem(mediaItem)
                prepare()
                
                // Add listener for position updates
                addListener(object : Player.Listener {
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
    }
    
    // Cleanup ExoPlayer on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
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
                                
                                // Handle play/pause based on isPlaying state
                                if (isPlaying) {
                                    exoPlayer?.play()
                                } else {
                                    exoPlayer?.pause()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to episode thumbnail
                    AsyncImage(
                        model = episode.imageUrl,
                        contentDescription = "Episode ${episode.episodeNumber}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Control buttons and progress bar...
        }
    }
}
```

## Usage Examples

### 1. Fetching Episodes for an Anime

```kotlin
// In your ViewModel or Repository
val episodesResult = storageRepository.getAnimeEpisodes("attack_on_titan")
if (episodesResult.isSuccess) {
    val episodes = episodesResult.getOrNull()
    // Use episodes with video URLs
} else {
    // Handle error
    val error = episodesResult.exceptionOrNull()
}
```

### 2. Getting a Specific Episode URL

```kotlin
val videoUrlResult = storageRepository.getEpisodeVideoUrl("attack_on_titan", 1)
if (videoUrlResult.isSuccess) {
    val videoUrl = videoUrlResult.getOrNull()
    // Use video URL for playback
}
```

### 3. Checking Available Anime

```kotlin
val animeListResult = storageRepository.getAvailableAnimeList()
if (animeListResult.isSuccess) {
    val availableAnime = animeListResult.getOrNull()
    // Display list of available anime
}
```

### 4. Checking if Episode Exists

```kotlin
val existsResult = storageRepository.episodeExists("attack_on_titan", 5)
if (existsResult.isSuccess) {
    val exists = existsResult.getOrNull()
    if (exists == true) {
        // Episode is available
    }
}
```

## File Naming Conventions

The system supports various file naming patterns:

- `episode_1.mp4`
- `ep1.mp4`
- `01.mp4`
- `e1.mp4`

## Supported Video Formats

- MP4 (.mp4)
- AVI (.avi)
- MKV (.mkv)
- MOV (.mov)
- WMV (.wmv)
- FLV (.flv)
- WebM (.webm)

## Error Handling

The implementation includes comprehensive error handling:

```kotlin
// Example error handling
try {
    val episodes = storageRepository.getAnimeEpisodes(animeId)
    if (episodes.isSuccess) {
        // Handle success
    } else {
        // Handle failure
        val error = episodes.exceptionOrNull()
        Log.e("TAG", "Error fetching episodes: ${error?.message}")
    }
} catch (e: Exception) {
    // Handle unexpected errors
    Log.e("TAG", "Unexpected error: ${e.message}")
}
```

## Performance Optimizations

1. **Lazy Loading**: Episodes are fetched only when needed
2. **Caching**: ExoPlayer handles video caching automatically
3. **Error Recovery**: Graceful fallback to database episodes if storage fails
4. **Memory Management**: Proper ExoPlayer cleanup to prevent memory leaks

## Security Considerations

1. **Firebase Storage Rules**: Ensure proper security rules are configured
2. **Authentication**: Verify user permissions before accessing content
3. **URL Expiration**: Handle expired download URLs gracefully

## Dependencies Required

```kotlin
// Add to build.gradle.kts
implementation("androidx.media3:media3-exoplayer:1.2.1")
implementation("androidx.media3:media3-ui:1.2.1")
implementation("androidx.media3:media3-common:1.2.1")
```

## Conclusion

This Firebase Storage integration provides a robust, scalable solution for serving anime episodes. It automatically discovers content, handles various file formats, and integrates seamlessly with your existing media player. The implementation includes proper error handling, performance optimizations, and maintains compatibility with your existing database structure. 