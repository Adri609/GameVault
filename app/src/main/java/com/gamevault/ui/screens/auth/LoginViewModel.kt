package com.gamevault.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.domain.usecase.LoginWithEmailUseCase
import com.gamevault.domain.usecase.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de autenticación.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel que gestiona la lógica de autenticación (Email y Google).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Actualiza el correo en el estado. */
    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    /** Actualiza la contraseña en el estado. */
    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    /** Actualiza el nombre de usuario en el estado. */
    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    /**
     * Proceso de registro con email y contraseña.
     */
    fun onRegisterClicked() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank() || currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, rellena todos los campos.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            loginWithEmailUseCase(
                email = currentState.email,
                pass = currentState.password,
                username = currentState.username
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error desconocido en el registro."
                    )
                }
            }
        }
    }

    /**
     * Proceso de inicio de sesión con Google.
     * @param idToken Token obtenido de Google Sign-In.
     */
    fun onGoogleLoginTokenReceived(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            loginWithGoogleUseCase(idToken)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al iniciar sesión con Google."
                        )
                    }
                }
        }
    }

    /** Limpia los errores del estado. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
