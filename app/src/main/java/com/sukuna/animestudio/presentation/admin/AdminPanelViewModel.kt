package com.sukuna.animestudio.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.domain.UserManager
import com.sukuna.animestudio.domain.model.Anime
import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    val dbRepository: DbRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _animes = MutableStateFlow<List<Anime>>(emptyList())
    val animes: StateFlow<List<Anime>> = _animes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchUsers() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _users.value = dbRepository.getAllUsers()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load users."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserRole(user: User, newRole: UserRole, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val updated = user.copy(role = newRole)
            val success = dbRepository.updateUser(updated)
            if (success) {
                _users.update { list -> list.map { if (it.id == user.id) updated else it } }
                
                // Update shared UserManager if this is the current user
                val currentUser = userManager.currentUser.value
                if (currentUser?.id == user.id) {
                    userManager.updateUserRole(newRole)
                }
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    fun deleteUser(user: User, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = dbRepository.deleteUser(user.id)
            if (success) {
                _users.update { list -> list.filterNot { it.id == user.id } }
                
                // Clear current user if they were deleted
                val currentUser = userManager.currentUser.value
                if (currentUser?.id == user.id) {
                    userManager.clearUser()
                }
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    fun fetchAnimes() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _animes.value = dbRepository.getAllAnimes()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load anime."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAnime(anime: Anime, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = dbRepository.addAnime(anime).isNotEmpty()
            if (success) {
                fetchAnimes() // Refresh the list
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    fun updateAnime(anime: Anime, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = dbRepository.updateAnime(anime)
            if (success) {
                _animes.update { list -> list.map { if (it.id == anime.id) anime else it } }
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    fun deleteAnime(anime: Anime, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val success = dbRepository.deleteAnime(anime.id)
            if (success) {
                _animes.update { list -> list.filterNot { it.id == anime.id } }
            }
            _isLoading.value = false
            onResult(success)
        }
    }
}