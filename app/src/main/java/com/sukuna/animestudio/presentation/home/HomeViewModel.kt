package com.sukuna.animestudio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.domain.RoleManager
import com.sukuna.animestudio.domain.UserManager
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
    private val roleManager: RoleManager,
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
}

data class HomeUiState(
    val user: User? = null
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
