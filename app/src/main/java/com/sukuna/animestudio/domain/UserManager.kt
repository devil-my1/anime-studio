package com.sukuna.animestudio.domain

import com.sukuna.animestudio.domain.model.User
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

    fun clearUser() {
        _currentUser.value = null
    }
} 