package com.sukuna.animestudio.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.StorageRepository
import com.sukuna.animestudio.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    _uiState.update { state ->
                        state.copy(
                            user = User(
                                id = currentUser.uid,
                                email = currentUser.email ?: "",
                                username = currentUser.displayName ?: "User",
                                profilePictureUrl = currentUser.photoUrl?.toString() ?: "",
                                bio = "Welcome to my profile!",
                                favoriteAnime = listOf("Naruto", "One Piece", "Attack on Titan"),
                                watchlist = listOf(
                                    "Jujutsu Kaisen",
                                    "Demon Slayer",
                                    "My Hero Academia"
                                ),
                                completedAnime = listOf(
                                    "Death Note",
                                    "Fullmetal Alchemist",
                                    "Steins;Gate"
                                ),
                                watchingAnime = listOf(
                                    "One Punch Man",
                                    "Tokyo Ghoul",
                                    "Hunter x Hunter"
                                ),
                                droppedAnime = listOf("Bleach", "Fairy Tail", "Sword Art Online")
                            ),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProfile(username: String, bio: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TODO: Implement actual profile update in Firestore
                _uiState.update { state ->
                    state.copy(
                        user = state.user.copy(
                            username = username,
                            bio = bio
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    storageRepository.uploadProfilePicture(currentUser.uid, imageUri)
                        .onSuccess { downloadUrl ->
                            _uiState.update { state ->
                                state.copy(
                                    user = state.user.copy(profilePictureUrl = downloadUrl),
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { state ->
                                state.copy(
                                    error = error.message,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    storageRepository.deleteProfilePicture(currentUser.uid)
                        .onSuccess {
                            _uiState.update { state ->
                                state.copy(
                                    user = state.user.copy(profilePictureUrl = ""),
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update { state ->
                                state.copy(
                                    error = error.message,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update { it.copy(isSignedOut = true) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedOut: Boolean = false
) 