package com.sukuna.animestudio.presentation.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
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
    private val storageRepository: StorageRepository,
    private val dbRepository: DbRepository
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
                    val userData = dbRepository.getUserById(currentUser.uid)
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
                _uiState.update { state ->
                    state.copy(
                        user = state.user?.copy(
                            username = username,
                            bio = bio
                        ),
                        isLoading = false
                    )
                }
                val updRes = dbRepository.updateUser(_uiState.value.user!!)
                if (updRes > 0) {
                    _uiState.update { state ->
                        state.copy(
                            error = null,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            error = "Failed to update profile",
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
    val user: User? = User(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedOut: Boolean = false
) 