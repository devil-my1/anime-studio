package com.sukuna.animestudio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import com.sukuna.animestudio.data.repository.DbRepository
import com.sukuna.animestudio.domain.RoleManager
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
    private val roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            val userData = if (currentUser != null) {
                dbRepository.getUserById(currentUser.uid)
            } else null
            _uiState.update { state ->
                state.copy(user = userData)
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
}
