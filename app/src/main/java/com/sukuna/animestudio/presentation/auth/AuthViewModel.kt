package com.sukuna.animestudio.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukuna.animestudio.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AuthUiState()
    )

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onSignInClick() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password.trim()
            if (email.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(error = "Email and password cannot be empty") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signIn(email, password)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isAuthenticated = true
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
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

    fun onSignUpClick() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password.trim()
            if (email.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(error = "Email and password cannot be empty") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signUp(email, password)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isAuthenticated = true
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
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

    /**
     * Allow the user to bypass authentication and use the app as a guest.
     * This simply marks the UI state as authenticated so navigation proceeds
     * to the home screen where a null user maps to the GUEST role.
     */
    fun continueAsGuest() {
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
) 