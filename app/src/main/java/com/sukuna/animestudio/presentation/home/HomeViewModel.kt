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

                // Simple categorization based on existing fields. This can be
                // adjusted as your database schema evolves.
                val featured = animes.sortedByDescending { it.rating }.take(3)
                val trending = animes.filter { it.animeStatus == AnimeStatus.IN_PROGRESS }
                val favorites = animes.filter { it.isFavorite }
                val top10 = animes.sortedByDescending { it.rating }.take(10)
                val allTime = animes.filter { it.animeStatus == AnimeStatus.COMPLETED }
                val nextSeason = animes.filter { it.animeStatus == AnimeStatus.SOON_ARRIVING }

                _uiState.update { state ->
                    state.copy(
                        featuredAnime = featured,
                        trendingAnime = trending,
                        mostFavoriteAnime = favorites,
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

    fun onAnimeFavoriteToggle(anime: Anime) {
        viewModelScope.launch {
            // Update favorite status in repository
            // For now, just update local state
            _uiState.update { state ->
                state.copy(
                    featuredAnime = state.featuredAnime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    },
                    trendingAnime = state.trendingAnime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    },
                    mostFavoriteAnime = state.mostFavoriteAnime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    },
                    top10Anime = state.top10Anime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    },
                    allTimePopularAnime = state.allTimePopularAnime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    },
                    nextSeasonAnime = state.nextSeasonAnime.map { 
                        if (it.id == anime.id) it.copy(isFavorite = !it.isFavorite) else it 
                    }
                )
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
