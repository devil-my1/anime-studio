package com.sukuna.animestudio.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
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
 * Handles anime data loading, episode selection, and media player state management.
 * Also manages user's watching state and updates the database accordingly.
 */
@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val dbRepository: DbRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
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
        loadAnimeDetails()
    }

    /**
     * Loads the detailed anime information and episodes from the database.
     * Handles loading states and error scenarios gracefully.
     */
    private fun loadAnimeDetailsInternal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val anime = dbRepository.getAnimeById(animeId)
                if (anime != null) {
                    _uiState.update { state ->
                        state.copy(
                            anime = anime,
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

    init {
        loadAnimeDetailsInternal()
    }

    /**
     * Public method to reload anime details (used for retry functionality).
     */
    fun loadAnimeDetails() {
        loadAnimeDetailsInternal()
    }

    /**
     * Selects an episode and prepares the media player for playback.
     * Also adds the anime to the user's watching list if not already present.
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
                        // Check if anime is already in watching list
                        val isAlreadyWatching = userData.watchingAnime.any { it.id == anime.id }
                        
                        if (!isAlreadyWatching) {
                            // Add anime to watching list
                            val updatedWatchingList = userData.watchingAnime + anime
                            val updatedUser = userData.copy(watchingAnime = updatedWatchingList)
                            
                            // Update in database
                            val success = dbRepository.updateUser(updatedUser)
                            
                            if (success) {
                                // Update UserManager for real-time updates
                                userManager.updateCurrentUser(updatedUser)
                            }
                        }
                    }
                }
                
                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        selectedEpisode = episode,
                        isPlaying = false,
                        currentPosition = 0L,
                        isFullScreen = false
                    )
                }
            } catch (e: Exception) {
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
                        val totalEpisodes = anime.episodes.size
                        val watchedEpisodes = anime.episodes.count { it.isWatched } + 1 // +1 for current episode
                        
                        // Check if this is the last episode
                        val isCompleted = watchedEpisodes >= totalEpisodes
                        
                        var updatedUser = userData
                        
                        if (isCompleted) {
                            // Move anime from watching to completed list
                            val updatedWatchingList = userData.watchingAnime.filter { it.id != anime.id }
                            val updatedCompletedList = userData.completedAnime + anime
                            
                            updatedUser = userData.copy(
                                watchingAnime = updatedWatchingList,
                                completedAnime = updatedCompletedList
                            )
                        }
                        
                        // Update in database
                        val success = dbRepository.updateUser(updatedUser)
                        
                        if (success) {
                            // Update UserManager for real-time updates
                            userManager.updateCurrentUser(updatedUser)
                        }
                    }
                }
                
                // Update the local anime state to reflect watched episode
                _uiState.update { state ->
                    val updatedEpisodes = state.anime?.episodes?.map { 
                        if (it.id == episode.id) episode.copy(isWatched = true) else it 
                    } ?: emptyList()
                    
                    state.copy(
                        anime = state.anime?.copy(episodes = updatedEpisodes)
                    )
                }
            } catch (e: Exception) {
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