package com.sukuna.animestudio.domain

import com.sukuna.animestudio.domain.model.User
import com.sukuna.animestudio.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun updateCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun updateUserRole(newRole: com.sukuna.animestudio.domain.model.UserRole) {
        _currentUser.update { user ->
            user?.copy(role = newRole)
        }
    }

    /**
     * Updates the user's favorite anime list and notifies all observers
     * @param favoriteAnime The updated list of favorite anime
     */
    fun updateUserFavorites(favoriteAnime: List<com.sukuna.animestudio.domain.model.Anime>) {
        _currentUser.update { user ->
            user?.copy(favoriteAnime = favoriteAnime)
        }
    }

    fun clearUser() {
        _currentUser.value = null
    }

} 