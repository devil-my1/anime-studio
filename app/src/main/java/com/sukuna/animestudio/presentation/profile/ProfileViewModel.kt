package com.sukuna.animestudio.presentation.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.data.repository.StorageRepository
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.UserManager
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
    private val storageRepository: StorageRepository,
    private val dbRepository: DbRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    // Expose UserManager's currentUser for real-time updates
    val currentUser: StateFlow<User?> = userManager.currentUser

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    // First try to get from UserManager (for real-time updates)
                    val userData = userManager.currentUser.value ?: dbRepository.getUserById(currentUser.uid)
                    
                    // Update UserManager if we fetched from DB
                    if (userData != null && userManager.currentUser.value == null) {
                        userManager.updateCurrentUser(userData)
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            user = userData,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading user profile: ${e.message}")
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
                val updatedUser = _uiState.value.user?.copy(
                    username = username,
                    bio = bio
                )
                
                if (updatedUser != null) {
                    val updRes = dbRepository.updateUser(updatedUser)
                    if (updRes) {
                        // Update both local state and UserManager
                        _uiState.update { state ->
                            state.copy(
                                user = updatedUser,
                                error = null,
                                isLoading = false
                            )
                        }
                        userManager.updateCurrentUser(updatedUser)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                error = "Failed to update profile",
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
                                    user = state.user?.copy(profilePictureUrl = downloadUrl),
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
                                    user = state.user?.copy(profilePictureUrl = ""),
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
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedOut: Boolean = false
)
