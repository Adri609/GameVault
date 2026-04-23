package com.gamevault.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.domain.usecase.RegisterWithEmailUseCase
import com.gamevault.domain.usecase.LoginWithGoogleUseCase
import com.gamevault.domain.usecase.SignInWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de registro y autenticación.
 */
data class RegisterUiState(
    val isLoginMode: Boolean = false, // Por defecto en modo registro según la petición de refactorización
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel que gestiona la lógica de registro y autenticación (Email y Google).
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun toggleLoginMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null,
                password = ""
            )
        }
    }

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
     * Proceso principal de registro/login
     */
    fun onRegisterClicked() {
        val currentState = _uiState.value

        // Validación básica
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El correo y la contraseña son obligatorios.") }
            return
        }

        // Validación extra solo si es registro
        if (!currentState.isLoginMode && currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, introduce un nombre de usuario.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (currentState.isLoginMode) {
                // Ejecutar inicio de sesión
                signInWithEmailUseCase(currentState.email, currentState.password)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    }
            } else {
                // Ejecutar registro
                registerWithEmailUseCase(currentState.email, currentState.password, currentState.username)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    }
            }
        }
    }

    /**
     * Proceso de inicio de sesión con Google.
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
