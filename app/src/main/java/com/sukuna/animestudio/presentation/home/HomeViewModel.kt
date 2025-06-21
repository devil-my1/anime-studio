package com.sukuna.animestudio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.domain.RoleManager
import com.sukuna.animestudio.domain.UserManager
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.AnimeStatus
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dbRepository: DbRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    // Expose UserManager's currentUser for real-time updates
    val currentUser: StateFlow<User?> = userManager.currentUser

    // Store all anime from database to maintain consistency in mostFavoriteAnime list
    private var allAnimeFromDb: List<Anime> = emptyList()

    init {
        loadUser()
        loadAnimeData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                // First try to get from UserManager (for real-time updates)
                val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                
                // Update UserManager if we fetched from DB
                if (userData != null && userManager.currentUser.value == null) {
                    userManager.updateCurrentUser(userData)
                }
                
                _uiState.update { state ->
                    state.copy(user = userData)
                }
                
                // Refresh anime data to update favorite status based on current user
                if (userData != null) {
                    refreshAnimeDataWithUserFavorites(userData)
                }
            }
        }
    }

    /**
     * Refreshes anime data with updated favorite status based on current user
     */
    private fun refreshAnimeDataWithUserFavorites(currentUser: User) {
        viewModelScope.launch {
            try {
                // Get favorite statistics from all users
                val favoriteStatistics = dbRepository.getFavoriteStatistics()
                val currentUserFavoriteIds = currentUser.favoriteAnime.map { it.id }.toSet()
                
                // Update the complete anime list with favorite statistics and user status
                val updatedAllAnimeFromDb = allAnimeFromDb.map { anime ->
                    val favoriteCount = favoriteStatistics[anime.id] ?: 0
                    val isFavorite = currentUserFavoriteIds.contains(anime.id)
                    anime.copy(
                        favoriteCount = favoriteCount,
                        isFavorite = isFavorite
                    )
                }
                allAnimeFromDb = updatedAllAnimeFromDb

                // Update UI state with refreshed anime data
                _uiState.update { state ->
                    val featured = updatedAllAnimeFromDb.sortedByDescending { it.rating }.take(3)
                    val trending = updatedAllAnimeFromDb.filter { it.animeStatus == AnimeStatus.IN_PROGRESS }
                    val mostFavoriteAnime = updatedAllAnimeFromDb
                        .filter { anime -> anime.favoriteCount > 0 }
                        .sortedByDescending { anime -> anime.favoriteCount }
                        .take(10)
                    val top10 = updatedAllAnimeFromDb.sortedByDescending { it.rating }.take(10)
                    val allTime = updatedAllAnimeFromDb.filter { it.animeStatus == AnimeStatus.COMPLETED }
                    val nextSeason = updatedAllAnimeFromDb.filter { it.animeStatus == AnimeStatus.SOON_ARRIVING }
                    
                    state.copy(
                        featuredAnime = featured,
                        trendingAnime = trending,
                        mostFavoriteAnime = mostFavoriteAnime,
                        top10Anime = top10,
                        allTimePopularAnime = allTime,
                        nextSeasonAnime = nextSeason
                    )
                }
            } catch (e: Exception) {
                // Handle error silently for refresh operations
                // The main loadAnimeData will handle initial loading errors
            }
        }
    }

    /**
     * Fetch all anime entries from Firestore and populate each section.
     * This runs on a background coroutine and updates the UI state when done.
     */
    private fun loadAnimeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val animes = dbRepository.getAllAnimes()
                
                // Get favorite statistics from all users to calculate community favorites
                val favoriteStatistics = dbRepository.getFavoriteStatistics()
                
                // Get current user's favorite anime IDs for individual favorite status
                val currentUser = _uiState.value.user
                val currentUserFavoriteIds = currentUser?.favoriteAnime?.map { it.id } ?: emptySet()
                
                // Enhance anime data with favorite statistics and current user's favorite status
                val enhancedAnimes = animes.map { anime ->
                    val favoriteCount = favoriteStatistics[anime.id] ?: 0
                    val isFavorite = currentUserFavoriteIds.contains(anime.id)
                    anime.copy(
                        favoriteCount = favoriteCount,
                        isFavorite = isFavorite
                    )
                }
                
                // Store the complete enhanced anime list for consistent favorite management
                allAnimeFromDb = enhancedAnimes

                // Simple categorization based on existing fields. This can be
                // adjusted as your database schema evolves.
                val featured = enhancedAnimes.sortedByDescending { it.rating }.take(3)
                val trending = enhancedAnimes.filter { it.animeStatus == AnimeStatus.IN_PROGRESS }
                
                // Calculate most favorite anime based on community statistics
                val mostFavoriteAnime = enhancedAnimes
                    .filter { anime -> anime.favoriteCount > 0 }
                    .sortedByDescending { anime -> anime.favoriteCount }
                    .take(10) // Show top 10 most favorite anime
                
                val top10 = enhancedAnimes.sortedByDescending { it.rating }.take(10)
                val allTime = enhancedAnimes.filter { it.animeStatus == AnimeStatus.COMPLETED }
                val nextSeason = enhancedAnimes.filter { it.animeStatus == AnimeStatus.SOON_ARRIVING }

                _uiState.update { state ->
                    state.copy(
                        featuredAnime = featured,
                        trendingAnime = trending,
                        mostFavoriteAnime = mostFavoriteAnime,
                        top10Anime = top10,
                        allTimePopularAnime = allTime,
                        nextSeasonAnime = nextSeason,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onAnimeClick(anime: Anime) {
        // Handle anime click - navigate to detail screen
        _uiState.update { state ->
            state.copy(selectedAnime = anime)
        }
    }

    /**
     * Toggles the favorite status of an anime and updates both the anime state
     * and the user's favorite list in the database.
     * 
     * When marking as favorite: Adds anime to user's favoriteAnime list
     * When unmarking favorite: Removes anime from user's favoriteAnime list
     */
    fun onAnimeFavoriteToggle(anime: Anime) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user
                if (currentUser == null) {
                    // Handle case where user is not logged in
                    _uiState.update { state ->
                        state.copy(error = "User must be logged in to manage favorites")
                    }
                    return@launch
                }

                // Determine if we're adding or removing from favorites
                val isCurrentlyFavorite = currentUser.favoriteAnime.any { it.id == anime.id }
                val newFavoriteStatus = !isCurrentlyFavorite

                // Update user's favorite list
                val updatedFavoriteAnime = if (newFavoriteStatus) {
                    // Add to favorites if not already present
                    if (!isCurrentlyFavorite) {
                        currentUser.favoriteAnime + anime
                    } else {
                        currentUser.favoriteAnime
                    }
                } else {
                    // Remove from favorites
                    currentUser.favoriteAnime.filter { it.id != anime.id }
                }

                // Update user favorites in database (more efficient than updating entire user)
                val updateSuccess = dbRepository.updateUserFavorites(currentUser.id, updatedFavoriteAnime)
                if (!updateSuccess) {
                    _uiState.update { state ->
                        state.copy(error = "Failed to update favorites in database")
                    }
                    return@launch
                }

                // Create updated user with new favorite list
                val updatedUser = currentUser.copy(favoriteAnime = updatedFavoriteAnime)

                // Update local user state in UserManager for real-time updates
                userManager.updateCurrentUser(updatedUser)

                // Refresh favorite statistics and update anime data
                val updatedFavoriteStatistics = dbRepository.getFavoriteStatistics()
                val updatedUserFavoriteIds = updatedUser.favoriteAnime.map { it.id }.toSet()
                
                // Update the complete anime list with new favorite statistics and user status
                val updatedAllAnimeFromDb = allAnimeFromDb.map { anime ->
                    val newFavoriteCount = updatedFavoriteStatistics[anime.id] ?: 0
                    val newIsFavorite = updatedUserFavoriteIds.contains(anime.id)
                    anime.copy(
                        favoriteCount = newFavoriteCount,
                        isFavorite = newIsFavorite
                    )
                }
                allAnimeFromDb = updatedAllAnimeFromDb

                // Update UI state with new user and anime data
                _uiState.update { state ->
                    // Update the isFavorite property across all anime lists
                    val updatedFeaturedAnime = state.featuredAnime.map { 
                        if (it.id == anime.id) {
                            val newFavoriteCount = updatedFavoriteStatistics[it.id] ?: 0
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                favoriteCount = newFavoriteCount
                            )
                        } else it 
                    }
                    val updatedTrendingAnime = state.trendingAnime.map { 
                        if (it.id == anime.id) {
                            val newFavoriteCount = updatedFavoriteStatistics[it.id] ?: 0
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                favoriteCount = newFavoriteCount
                            )
                        } else it 
                    }
                    val updatedTop10Anime = state.top10Anime.map { 
                        if (it.id == anime.id) {
                            val newFavoriteCount = updatedFavoriteStatistics[it.id] ?: 0
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                favoriteCount = newFavoriteCount
                            )
                        } else it 
                    }
                    val updatedAllTimePopularAnime = state.allTimePopularAnime.map { 
                        if (it.id == anime.id) {
                            val newFavoriteCount = updatedFavoriteStatistics[it.id] ?: 0
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                favoriteCount = newFavoriteCount
                            )
                        } else it 
                    }
                    val updatedNextSeasonAnime = state.nextSeasonAnime.map { 
                        if (it.id == anime.id) {
                            val newFavoriteCount = updatedFavoriteStatistics[it.id] ?: 0
                            it.copy(
                                isFavorite = newFavoriteStatus,
                                favoriteCount = newFavoriteCount
                            )
                        } else it 
                    }
                    
                    // Update mostFavoriteAnime based on the updated favorite counts
                    val updatedMostFavoriteAnime = updatedAllAnimeFromDb
                        .filter { anime -> anime.favoriteCount > 0 }
                        .sortedByDescending { anime -> anime.favoriteCount }
                        .take(10)
                    
                    state.copy(
                        user = updatedUser,
                        featuredAnime = updatedFeaturedAnime,
                        trendingAnime = updatedTrendingAnime,
                        mostFavoriteAnime = updatedMostFavoriteAnime,
                        top10Anime = updatedTop10Anime,
                        allTimePopularAnime = updatedAllTimePopularAnime,
                        nextSeasonAnime = updatedNextSeasonAnime,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = "Error updating favorites: ${e.message}")
                }
            }
        }
    }

}

data class HomeUiState(
    val user: User? = null,
    val featuredAnime: List<Anime> = emptyList(),
    val trendingAnime: List<Anime> = emptyList(),
    val mostFavoriteAnime: List<Anime> = emptyList(),
    val top10Anime: List<Anime> = emptyList(),
    val allTimePopularAnime: List<Anime> = emptyList(),
    val nextSeasonAnime: List<Anime> = emptyList(),
    val selectedAnime: Anime? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val role: UserRole
        get() = user?.role ?: UserRole.GUEST

    fun canEditAnime(roleManager: RoleManager) =
        roleManager.canEditAnime(user)

    fun canManageUsers(roleManager: RoleManager) =
        roleManager.canManageUsers(user)

    fun canModerateContent(roleManager: RoleManager) =
        roleManager.canModerateContent(user)

    fun isGuest(roleManager: RoleManager) =
        roleManager.isGuest(user)
}
