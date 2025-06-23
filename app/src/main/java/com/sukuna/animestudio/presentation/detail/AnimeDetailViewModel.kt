package com.sukuna.animestudio.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.data.repository.StorageRepository
import com.sukuna.animestudio.domain.UserManager
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.Episode
import com.sukuna.animestudio.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the Anime Detail Screen state and business logic.
 * Handles anime data loading, episode selection, media player state management,
 * and Firebase Storage integration for video content.
 */
@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val dbRepository: DbRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val storageRepository: StorageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Extract anime ID from navigation arguments
    private val animeId: String = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AnimeDetailUiState()
    )

    // Expose current user for real-time updates
    val currentUser: StateFlow<User?> = userManager.currentUser

    init {
        loadAnimeDetailsInternal()
    }

    /**
     * Loads the detailed anime information and episodes from the database.
     * Also fetches episodes from Firebase Storage if available and saves them to database.
     * Handles loading states and error scenarios gracefully.
     */
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
                        
                        // Save new episodes from storage to database
                        saveStorageEpisodesToDatabase(anime, storageEpisodes)
                        
                        // Merge database episodes with storage episodes
                        val mergedEpisodes = mergeEpisodes(anime.episodes, storageEpisodes)
                        
                        anime.copy(episodes = mergedEpisodes)
                    } else {
                        // If Firebase Storage fails, use database episodes
                        Log.w("AnimeDetailViewModel", "Failed to fetch episodes from storage: ${storageEpisodesResult.exceptionOrNull()?.message}")
                        anime
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            anime = updatedAnime,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = "Anime not found"
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
     * Saves episodes found in Firebase Storage to the database.
     * Only saves episodes that don't already exist in the database.
     */
    private suspend fun saveStorageEpisodesToDatabase(anime: Anime, storageEpisodes: List<Episode>) {
        try {
            val existingEpisodeNumbers = anime.episodes.map { it.episodeNumber }.toSet()
            val newEpisodes = storageEpisodes.filter { !existingEpisodeNumbers.contains(it.episodeNumber) }
            
            if (newEpisodes.isNotEmpty()) {
                Log.d("AnimeDetailViewModel", "Found ${newEpisodes.size} new episodes in storage to save to database")
                
                // Save each new episode to the database
                newEpisodes.forEach { episode ->
                    try {
                        val success = dbRepository.updateEpisode(animeId, episode)
                        if (success) {
                            Log.d("AnimeDetailViewModel", "Successfully saved episode ${episode.episodeNumber} to database")
                        } else {
                            Log.w("AnimeDetailViewModel", "Failed to save episode ${episode.episodeNumber} to database")
                        }
                    } catch (e: Exception) {
                        Log.e("AnimeDetailViewModel", "Error saving episode ${episode.episodeNumber} to database: ${e.message}")
                    }
                }
                
                // Update the anime in the database with the new episodes
                val updatedEpisodes = anime.episodes + newEpisodes
                val updatedAnime = anime.copy(episodes = updatedEpisodes.sortedBy { it.episodeNumber })
                
                val animeUpdateSuccess = dbRepository.updateAnime(updatedAnime)
                if (animeUpdateSuccess) {
                    Log.d("AnimeDetailViewModel", "Successfully updated anime with ${newEpisodes.size} new episodes")
                } else {
                    Log.w("AnimeDetailViewModel", "Failed to update anime with new episodes")
                }
            } else {
                Log.d("AnimeDetailViewModel", "No new episodes found in storage to save")
            }
        } catch (e: Exception) {
            Log.e("AnimeDetailViewModel", "Error saving storage episodes to database: ${e.message}")
        }
    }

    /**
     * Public method to reload anime details (used for retry functionality).
     */
    fun loadAnimeDetails() {
        loadAnimeDetailsInternal()
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
                dbEpisode.copy(
                    videoUrl = storageEpisode.videoUrl,
                    // Keep other database fields (title, description, isWatched, etc.)
                )
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
                // Add storage episode to merged list
                mergedEpisodes.add(storageEpisode)
                Log.d("AnimeDetailViewModel", "Added storage episode ${storageEpisode.episodeNumber} that wasn't in database")
            }
        }
        
        // Sort by episode number
        val sortedEpisodes = mergedEpisodes.sortedBy { it.episodeNumber }
        Log.d("AnimeDetailViewModel", "Merged ${dbEpisodes.size} DB episodes with ${storageEpisodes.size} storage episodes. Total: ${sortedEpisodes.size}")
        
        return sortedEpisodes
    }

    /**
     * Selects an episode and prepares the media player for playback.
     * Also adds the anime to the user's watching list if not already present.
     * Fetches video URL from Firebase Storage if not already available.
     * @param episode The episode to be selected and played
     */
    fun selectEpisode(episode: Episode) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                val anime = _uiState.value.anime
                
                if (currentUser != null && anime != null) {
                    // Get current user data
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    if (userData != null) {
                        // Determine if the anime is already being watched
                        val isAlreadyWatching = userData.watchingAnime.any { it.id == anime.id }
                        val isInWatchlist = userData.watchlist.any { it.id == anime.id }

                        var updatedUser = userData

                        if (!isAlreadyWatching) {
                            val updatedWatchingList = userData.watchingAnime + anime
                            updatedUser = updatedUser.copy(watchingAnime = updatedWatchingList)
                        }

                        if (isInWatchlist) {
                            val updatedWatchlist = userData.watchlist.filter { it.id != anime.id }
                            updatedUser = updatedUser.copy(watchlist = updatedWatchlist)
                        }

                        if (updatedUser != userData) {
                            val success = dbRepository.updateUser(updatedUser)
                            if (success) {
                                userManager.updateCurrentUser(updatedUser)
                            }
                        }
                    }
                }
                
                // Check if episode has video URL, if not try to fetch from storage
                var updatedEpisode = episode
                if (episode.videoUrl.isEmpty()) {
                    val videoUrlResult = storageRepository.getEpisodeVideoUrl(animeId, episode.episodeNumber)
                    if (videoUrlResult.isSuccess) {
                        updatedEpisode = episode.copy(videoUrl = videoUrlResult.getOrNull() ?: "")
                        
                        // Update the episode in the anime's episode list
                        _uiState.update { state ->
                            val updatedEpisodes = state.anime?.episodes?.map { 
                                if (it.id == episode.id) updatedEpisode else it 
                            } ?: emptyList()
                            
                            state.copy(
                                anime = state.anime?.copy(episodes = updatedEpisodes)
                            )
                        }
                    }
                }
                
                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        selectedEpisode = updatedEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        isFullScreen = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AnimeDetailViewModel", "Error selecting episode: ${e.message}")
                // Handle error silently or show a toast
            }
        }
    }

    /**
     * Toggles the play/pause state of the media player.
     */
    fun togglePlayPause() {
        _uiState.update { state ->
            state.copy(isPlaying = !state.isPlaying)
        }
    }

    /**
     * Updates the current playback position in the media player.
     * @param position The new position in milliseconds
     */
    fun updatePosition(position: Long) {
        _uiState.update { state ->
            state.copy(currentPosition = position)
        }
    }

    /**
     * Seeks to a specific position in the media player.
     * @param position The target position in milliseconds
     */
    fun seekTo(position: Long) {
        _uiState.update { state ->
            state.copy(currentPosition = position)
        }
    }

    /**
     * Toggles the full-screen mode of the media player.
     */
    fun toggleFullScreen() {
        _uiState.update { state ->
            state.copy(isFullScreen = !state.isFullScreen)
        }
    }

    /**
     * Marks an episode as watched and updates the user's watching progress.
     * If the episode is the last one, moves the anime to completed list.
     * @param episode The episode to mark as watched
     */
    fun markEpisodeAsWatched(episode: Episode) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                val anime = _uiState.value.anime
                
                if (currentUser != null && anime != null) {
                    // Get current user data
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    if (userData != null) {
                        // Determine completion after marking this episode
                        val updatedEpisodes = anime.episodes.map {
                            if (it.id == episode.id) it.copy(isWatched = true) else it
                        }

                        val watchedCount = updatedEpisodes.count { it.isWatched }
                        val isCompleted = watchedCount >= anime.episodes.size

                        var updatedUser = userData

                        if (isCompleted) {
                            // Move anime from watching list to completed list
                            val updatedWatchingList = userData.watchingAnime.filter { it.id != anime.id }
                            val updatedCompletedList = userData.completedAnime + anime
                            updatedUser = userData.copy(
                                watchingAnime = updatedWatchingList,
                                completedAnime = updatedCompletedList
                            )
                        }

                        val success = dbRepository.updateUser(updatedUser)
                        if (success) {
                            // Update UserManager for real-time updates
                            userManager.updateCurrentUser(updatedUser)
                            
                            // Only update UI state after successful database update
                            _uiState.update { state ->
                                state.copy(anime = state.anime?.copy(episodes = updatedEpisodes))
                            }
                        } else {
                            Log.w("AnimeDetailViewModel", "Failed to update user data when marking episode as watched")
                        }
                    } else {
                        Log.w("AnimeDetailViewModel", "User data not found when marking episode as watched")
                    }
                }
            } catch (e: Exception) {
                Log.e("AnimeDetailViewModel", "Error marking episode as watched: ${e.message}")
                // Handle error silently or show a toast
            }
        }
    }

    /**
     * Toggles the favorite status of the anime and updates the user's favorite list.
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                val anime = _uiState.value.anime
                
                if (currentUser != null && anime != null) {
                    // Get current user data
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    if (userData != null) {
                        val isCurrentlyFavorite = userData.favoriteAnime.any { it.id == anime.id }
                        
                        val updatedFavoriteList = if (isCurrentlyFavorite) {
                            // Remove from favorites
                            userData.favoriteAnime.filter { it.id != anime.id }
                        } else {
                            // Add to favorites
                            userData.favoriteAnime + anime
                        }
                        
                        val updatedUser = userData.copy(favoriteAnime = updatedFavoriteList)
                        
                        // Update in database
                        val success = dbRepository.updateUser(updatedUser)
                        
                        if (success) {
                            // Update UserManager for real-time updates
                            userManager.updateCurrentUser(updatedUser)
                            
                            // Update local anime state
                            _uiState.update { state ->
                                state.copy(
                                    anime = state.anime?.copy(isFavorite = !isCurrentlyFavorite)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AnimeDetailViewModel", "Error toggling favorite: ${e.message}")
                // Handle error silently or show a toast
            }
        }
    }

    /**
     * Removes anime from user's watching list and moves it to dropped list.
     */
    fun dropAnime() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                val anime = _uiState.value.anime
                
                if (currentUser != null && anime != null) {
                    // Get current user data
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    if (userData != null) {
                        // Move anime from watching to dropped list
                        val updatedWatchingList = userData.watchingAnime.filter { it.id != anime.id }
                        val updatedDroppedList = userData.droppedAnime + anime
                        
                        val updatedUser = userData.copy(
                            watchingAnime = updatedWatchingList,
                            droppedAnime = updatedDroppedList
                        )
                        
                        // Update in database
                        val success = dbRepository.updateUser(updatedUser)
                        
                        if (success) {
                            // Update UserManager for real-time updates
                            userManager.updateCurrentUser(updatedUser)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AnimeDetailViewModel", "Error dropping anime: ${e.message}")
                // Handle error silently or show a toast
            }
        }
    }

    /**
     * Adds anime to user's watchlist.
     */
    fun addToWatchlist() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                val anime = _uiState.value.anime
                
                if (currentUser != null && anime != null) {
                    // Get current user data
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    if (userData != null) {
                        // Check if already in watchlist
                        val isAlreadyInWatchlist = userData.watchlist.any { it.id == anime.id }
                        
                        if (!isAlreadyInWatchlist) {
                            val updatedWatchlist = userData.watchlist + anime
                            val updatedUser = userData.copy(watchlist = updatedWatchlist)
                            
                            // Update in database
                            val success = dbRepository.updateUser(updatedUser)
                            
                            if (success) {
                                // Update UserManager for real-time updates
                                userManager.updateCurrentUser(updatedUser)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AnimeDetailViewModel", "Error adding to watchlist: ${e.message}")
                // Handle error silently or show a toast
            }
        }
    }
}

/**
 * UI State class representing the complete state of the Anime Detail Screen.
 * Contains all necessary data for rendering the UI components.
 */
data class AnimeDetailUiState(
    val anime: Anime? = null,
    val selectedEpisode: Episode? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val isFullScreen: Boolean = false
) 