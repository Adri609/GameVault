package com.gamevault.ui.login

import androidx.lifecycle.ViewModel
import com.gamevault.domain.usecase.LoginWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de la pantalla
// Clase que contiene todo lo que la interfaz de compose necesita saber para pintarse
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false, // Para mostrar un indicador de carga
    val errorMessage: String? = null, // Si falla algo, se guarda el error aquí
    val isSuccess: Boolean = false // Si se registra bien, se cambia esto a true
)

// Cerebro
@HiltViewModel // Hilt inyecta automáticamente el useCase
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase
) : ViewModel() {
    // Compose observa el StateFlow y se redibuja solo cuando cambia.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Acciones del usuario

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    // Se llama cuando el usuario pulsa el botón de registrarse
    fun onRegisterClicked(){

    }
}