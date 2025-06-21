package com.sukuna.animestudio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.domain.RoleManager
import com.sukuna.animestudio.domain.UserManager
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.AnimeGenre
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

    private fun loadAnimeData() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    featuredAnime = getFeaturedAnime(),
                    trendingAnime = getTrendingAnime(),
                    mostFavoriteAnime = getMostFavoriteAnime(),
                    top10Anime = getTop10Anime(),
                    allTimePopularAnime = getAllTimePopularAnime(),
                    nextSeasonAnime = getNextSeasonAnime(),
                    isLoading = false
                )
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

    // Sample data generation methods
    private fun getFeaturedAnime(): List<Anime> = listOf(
        Anime(
            id = "1",
            title = "Jujutsu Kaisen",
            description = "A boy swallows a cursed talisman - the finger of a demon - and becomes cursed himself.",
            imageUrl = "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Jujutsu+Kaisen",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.SUPERNATURAL, AnimeGenre.FANTASY),
            rating = 8.6,
            episodesCount = 24,
            animeStatus = AnimeStatus.IN_PROGRESS,
            isFavorite = true
        ),
        Anime(
            id = "2",
            title = "Demon Slayer",
            description = "A family is attacked by demons and only two members survive.",
            imageUrl = "https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=Demon+Slayer",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.FANTASY, AnimeGenre.HISTORICAL),
            rating = 8.9,
            episodesCount = 26,
            animeStatus = AnimeStatus.COMPLETED,
            isFavorite = false
        ),
        Anime(
            id = "3",
            title = "Attack on Titan",
            description = "Humanity's last stand against giant humanoid creatures.",
            imageUrl = "https://via.placeholder.com/300x200/45B7D1/FFFFFF?text=Attack+on+Titan",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.DRAMA, AnimeGenre.FANTASY),
            rating = 9.0,
            episodesCount = 25,
            animeStatus = AnimeStatus.COMPLETED,
            isFavorite = true
        )
    )

    private fun getTrendingAnime(): List<Anime> = listOf(
        Anime(
            id = "4",
            title = "One Piece",
            description = "A pirate adventure to find the ultimate treasure.",
            imageUrl = "https://via.placeholder.com/300x200/96CEB4/FFFFFF?text=One+Piece",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.ADVENTURE, AnimeGenre.COMEDY),
            rating = 8.7,
            episodesCount = 1000,
            animeStatus = AnimeStatus.IN_PROGRESS
        ),
        Anime(
            id = "5",
            title = "My Hero Academia",
            description = "A world where people have superpowers called Quirks.",
            imageUrl = "https://via.placeholder.com/300x200/FFEAA7/FFFFFF?text=My+Hero+Academia",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.SUPERNATURAL, AnimeGenre.SCHOOL),
            rating = 8.4,
            episodesCount = 25,
            animeStatus = AnimeStatus.IN_PROGRESS
        ),
        Anime(
            id = "6",
            title = "Black Clover",
            description = "A young boy strives to become the Wizard King.",
            imageUrl = "https://via.placeholder.com/300x200/DDA0DD/FFFFFF?text=Black+Clover",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.FANTASY, AnimeGenre.MAGIC),
            rating = 7.9,
            episodesCount = 170,
            animeStatus = AnimeStatus.IN_PROGRESS
        ),
        Anime(
            id = "7",
            title = "Naruto Shippuden",
            description = "A ninja's journey to become the strongest.",
            imageUrl = "https://via.placeholder.com/300x200/98D8C8/FFFFFF?text=Naruto+Shippuden",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.ADVENTURE, AnimeGenre.FANTASY),
            rating = 8.3,
            episodesCount = 500,
            animeStatus = AnimeStatus.COMPLETED
        )
    )

    private fun getMostFavoriteAnime(): List<Anime> = listOf(
        Anime(
            id = "8",
            title = "Death Note",
            description = "A high school student finds a supernatural notebook.",
            imageUrl = "https://via.placeholder.com/300x200/F7DC6F/FFFFFF?text=Death+Note",
            genre = listOf(AnimeGenre.MYSTERY, AnimeGenre.PSYCHOLOGICAL, AnimeGenre.THRILLER),
            rating = 9.0,
            episodesCount = 37,
            animeStatus = AnimeStatus.COMPLETED,
            isFavorite = true
        ),
        Anime(
            id = "9",
            title = "Fullmetal Alchemist: Brotherhood",
            description = "Two brothers seek to restore their bodies using alchemy.",
            imageUrl = "https://via.placeholder.com/300x200/BB8FCE/FFFFFF?text=Fullmetal+Alchemist",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.ADVENTURE, AnimeGenre.FANTASY),
            rating = 9.1,
            episodesCount = 64,
            animeStatus = AnimeStatus.COMPLETED,
            isFavorite = true
        ),
        Anime(
            id = "10",
            title = "Hunter x Hunter",
            description = "A young boy becomes a Hunter to find his father.",
            imageUrl = "https://via.placeholder.com/300x200/85C1E9/FFFFFF?text=Hunter+x+Hunter",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.ADVENTURE, AnimeGenre.FANTASY),
            rating = 9.0,
            episodesCount = 148,
            animeStatus = AnimeStatus.COMPLETED,
            isFavorite = true
        )
    )

    private fun getTop10Anime(): List<Anime> = listOf(
        Anime(id = "11", title = "Steins;Gate", rating = 9.1, imageUrl = "https://via.placeholder.com/300x200/E8DAEF/FFFFFF?text=Steins;Gate"),
        Anime(id = "12", title = "Code Geass", rating = 8.9, imageUrl = "https://via.placeholder.com/300x200/FADBD8/FFFFFF?text=Code+Geass"),
        Anime(id = "13", title = "Cowboy Bebop", rating = 8.8, imageUrl = "https://via.placeholder.com/300x200/D5A6BD/FFFFFF?text=Cowboy+Bebop"),
        Anime(id = "14", title = "Neon Genesis Evangelion", rating = 8.7, imageUrl = "https://via.placeholder.com/300x200/A9CCE3/FFFFFF?text=Evangelion"),
        Anime(id = "15", title = "Ghost in the Shell", rating = 8.6, imageUrl = "https://via.placeholder.com/300x200/F9E79F/FFFFFF?text=Ghost+in+Shell"),
        Anime(id = "16", title = "Akira", rating = 8.5, imageUrl = "https://via.placeholder.com/300x200/D7BDE2/FFFFFF?text=Akira"),
        Anime(id = "17", title = "Spirited Away", rating = 8.4, imageUrl = "https://via.placeholder.com/300x200/A2D9CE/FFFFFF?text=Spirited+Away"),
        Anime(id = "18", title = "Princess Mononoke", rating = 8.3, imageUrl = "https://via.placeholder.com/300x200/F8C471/FFFFFF?text=Princess+Mononoke"),
        Anime(id = "19", title = "Your Name", rating = 8.2, imageUrl = "https://via.placeholder.com/300x200/85C1E9/FFFFFF?text=Your+Name"),
        Anime(id = "20", title = "A Silent Voice", rating = 8.1, imageUrl = "https://via.placeholder.com/300x200/BB8FCE/FFFFFF?text=A+Silent+Voice")
    )

    private fun getAllTimePopularAnime(): List<Anime> = listOf(
        Anime(
            id = "21",
            title = "Dragon Ball Z",
            description = "The legendary Saiyan warrior's epic battles.",
            imageUrl = "https://via.placeholder.com/300x200/F7DC6F/FFFFFF?text=Dragon+Ball+Z",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.ADVENTURE, AnimeGenre.FANTASY),
            rating = 8.7,
            episodesCount = 291,
            animeStatus = AnimeStatus.COMPLETED
        ),
        Anime(
            id = "22",
            title = "Pokemon",
            description = "A trainer's journey to become a Pokemon Master.",
            imageUrl = "https://via.placeholder.com/300x200/98D8C8/FFFFFF?text=Pokemon",
            genre = listOf(AnimeGenre.ADVENTURE, AnimeGenre.FANTASY, AnimeGenre.KIDS),
            rating = 7.5,
            episodesCount = 1000,
            animeStatus = AnimeStatus.IN_PROGRESS
        ),
        Anime(
            id = "23",
            title = "Sailor Moon",
            description = "Magical girls protect the world from evil.",
            imageUrl = "https://via.placeholder.com/300x200/DDA0DD/FFFFFF?text=Sailor+Moon",
            genre = listOf(AnimeGenre.MAGIC, AnimeGenre.ROMANCE, AnimeGenre.SHOUJO),
            rating = 7.8,
            episodesCount = 200,
            animeStatus = AnimeStatus.COMPLETED
        )
    )

    private fun getNextSeasonAnime(): List<Anime> = listOf(
        Anime(
            id = "24",
            title = "Chainsaw Man Season 2",
            description = "The return of the Chainsaw Devil.",
            imageUrl = "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Chainsaw+Man+S2",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.SUPERNATURAL, AnimeGenre.HORROR),
            rating = 0.0,
            episodesCount = 12,
            animeStatus = AnimeStatus.SOON_ARRIVING,
            releaseDate = "2024"
        ),
        Anime(
            id = "25",
            title = "Spy x Family Season 2",
            description = "A spy's mission to maintain world peace.",
            imageUrl = "https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=Spy+x+Family+S2",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.COMEDY, AnimeGenre.SLICE_OF_LIFE),
            rating = 0.0,
            episodesCount = 12,
            animeStatus = AnimeStatus.SOON_ARRIVING,
            releaseDate = "2024"
        ),
        Anime(
            id = "26",
            title = "Mob Psycho 100 Season 3",
            description = "A psychic's journey of self-discovery.",
            imageUrl = "https://via.placeholder.com/300x200/45B7D1/FFFFFF?text=Mob+Psycho+100+S3",
            genre = listOf(AnimeGenre.ACTION, AnimeGenre.SUPERNATURAL, AnimeGenre.COMEDY),
            rating = 0.0,
            episodesCount = 12,
            animeStatus = AnimeStatus.SOON_ARRIVING,
            releaseDate = "2024"
        )
    )
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
    val isLoading: Boolean = true
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
