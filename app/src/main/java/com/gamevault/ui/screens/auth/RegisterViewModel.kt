package com.gamevault.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamevault.domain.usecase.RegisterWithEmailUseCase
import com.gamevault.domain.usecase.LoginWithGoogleUseCase
import com.gamevault.domain.usecase.SignInWithEmailUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de registro, autenticación y recuperación de contraseña.
 *
 * @property isLoginMode Indica si la pantalla está en modo login (true) o registro (false)
 * @property email Email ingresado por el usuario
 * @property password Contraseña ingresada por el usuario
 * @property username Nombre de usuario (solo en modo registro)
 * @property isLoading Indica si hay una operación en progreso
 * @property errorMessage Mensaje de error a mostrar al usuario, si existe
 * @property isSuccess Indica si la operación fue exitosa
 * @property failedLoginAttempts Contador de intentos fallidos de inicio de sesión
 * @property isPasswordResetMode Indica si el usuario está en modo de restablecimiento de contraseña
 * @property passwordResetSent Indica si el correo de restablecimiento fue enviado exitosamente
 * @property resetCountdown Contador de segundos para el cooldown de reenvío (0-30)
 */
data class RegisterUiState(
    val isLoginMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val failedLoginAttempts: Int = 0,
    val isPasswordResetMode: Boolean = false,
    val passwordResetSent: Boolean = false,
    val resetCountdown: Int = 0
)

/**
 * ViewModel que gestiona la lógica de registro, autenticación (Email y Google) y recuperación de contraseña.
 *
 * Responsabilidades principales:
 * - Validación de formularios (Email, contraseña, usuario)
 * - Procesamiento de registro e inicio de sesión con email
 * - Autenticación con Google mediante Credential Manager
 * - Rastreo de intentos fallidos de login
 * - Gestión del flujo de recuperación de contraseña con cooldown
 * - Envío de correos de restablecimiento vía Firebase Auth
 *
 * @param registerWithEmailUseCase UseCase para registrar usuarios con email
 * @param loginWithGoogleUseCase UseCase para autenticar con Google
 * @param signInWithEmailUseCase UseCase para iniciar sesión con email
 * @param auth Cliente de FirebaseAuth para operaciones de autenticación
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())

    /**
     * Estado de la UI que refleja el estado actual de la pantalla de autenticación.
     * Este es un StateFlow que emite cambios de estado en tiempo real.
     */
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Alterna entre modo de inicio de sesión y modo de registro.
     * Limpia los campos de error, contraseña y reinicia los contadores relacionados.
     */
    fun toggleLoginMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null,
                password = "",
                failedLoginAttempts = 0,
                isPasswordResetMode = false,
                passwordResetSent = false,
                resetCountdown = 0
            )
        }
    }

    /**
     * Actualiza el correo en el estado.
     * @param newEmail Nuevo valor del correo
     */
    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    /**
     * Actualiza la contraseña en el estado.
     * @param newPassword Nuevo valor de la contraseña
     */
    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    /**
     * Actualiza el nombre de usuario en el estado.
     * @param newUsername Nuevo valor del nombre de usuario
     */
    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    /**
     * Proceso principal de registro/login.
     *
     * Realiza validaciones básicas y ejecuta el UseCase correspondiente.
     * En caso de error en login, rastrea los intentos fallidos.
     * Después de 3 intentos fallidos, muestra la opción de recuperar contraseña.
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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                failedLoginAttempts = 0,
                                isPasswordResetMode = false,
                                passwordResetSent = false
                            )
                        }
                    }
                    .onFailure { error ->
                        val newAttempts = currentState.failedLoginAttempts + 1
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message,
                                failedLoginAttempts = newAttempts
                            )
                        }
                    }
            } else {
                // Ejecutar registro
                registerWithEmailUseCase(currentState.email, currentState.password, currentState.username)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                failedLoginAttempts = 0,
                                isPasswordResetMode = false,
                                passwordResetSent = false
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
            }
        }
    }

    /**
     * Entra en modo de recuperación de contraseña.
     * Cambia la UI para mostrar solo el campo de email.
     */
    fun enterPasswordResetMode() {
        _uiState.update {
            it.copy(
                isPasswordResetMode = true,
                errorMessage = null,
                passwordResetSent = false
            )
        }
    }

    /**
     * Sale del modo de recuperación de contraseña.
     * Restaura la UI y limpia el estado de recuperación.
     */
    fun exitPasswordResetMode() {
        _uiState.update {
            it.copy(
                isPasswordResetMode = false,
                passwordResetSent = false,
                resetCountdown = 0,
                failedLoginAttempts = 0,
                errorMessage = null
            )
        }
    }

    /**
     * Envía un correo de recuperación de contraseña.
     * Valida el email y envía solicitud a Firebase Auth.
     */
    fun sendPasswordReset() {
        val email = _uiState.value.email

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Por favor, ingresa un correo válido.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        passwordResetSent = true,
                        resetCountdown = 30,
                        errorMessage = null
                    )
                }
                startResetCountdown()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al enviar el correo. Intenta de nuevo."
                    )
                }
            }
        }
    }

    /**
     * Inicia el contador de cooldown de 30 segundos para reenvío.
     * Decrementa el contador cada segundo.
     */
    private fun startResetCountdown() {
        viewModelScope.launch {
            repeat(30) {
                delay(1000)
                _uiState.update { state ->
                    state.copy(resetCountdown = maxOf(0, state.resetCountdown - 1))
                }
            }
        }
    }

    /**
     * Valida si el email tiene un formato válido.
     * @param email Email a validar
     * @return true si el email tiene formato válido
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Procesa el token de autenticación de Google.
     * @param idToken Token de ID obtenido del cliente de Credential Manager
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

    /**
     * Limpia el mensaje de error actual del estado.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
