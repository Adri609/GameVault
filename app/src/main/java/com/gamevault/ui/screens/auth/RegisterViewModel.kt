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
import kotlinx.coroutines.tasks.await

/**
 * Representa el estado inmutable de la interfaz de usuario para los flujos de Autenticación
 *
 * @property isLoginMode Determina si la UI debe renderizar el formulario de inicio de sesión (`true`) o de registro (`false`)
 * @property email Valor actual del campo de entrada de correo electrónico
 * @property password Valor actual del campo de entrada de contraseña
 * @property username Valor actual del campo de entrada de nombre de usuario (exclusivo del modo registro)
 * @property isLoading Bandera que indica si existe una transacción de red en progreso, utilizada para bloquear interacciones
 * @property errorMessage Mensaje de error descriptivo a mostrar al usuario. Es nulo si no hay errores activos
 * @property isSuccess Bandera que confirma la culminación exitosa de un flujo de autenticación, accionando la navegación
 * @property failedLoginAttempts Registro acumulativo de intentos fallidos de acceso. Superar el límite activa el modo de recuperación
 * @property isPasswordResetMode Determina si la UI debe mostrar el flujo de recuperación de contraseña
 * @property passwordResetSent Confirma si el enlace de restablecimiento fue despachado exitosamente al proveedor de correo
 * @property resetCountdown Temporizador activo en segundos que impide el reenvío abusivo de correos de recuperación
 * @property verificationEmailSent Confirma visualmente al usuario que se acaba de enviar el correo de validación tras un registro exitoso.
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
    val resetCountdown: Int = 0,
    val verificationEmailSent: Boolean = false
)

/**
 * ViewModel centralizado para la orquestación de la identidad del usuario
 *
 * Administra las validaciones locales de entrada y delega las operaciones complejas
 * a sus respectivos casos de uso (Inicio de sesión con Email, Registro y Autenticación federada con Google).
 * Además, implementa políticas de seguridad reactivas como el bloqueo por correo no verificado
 *
 * @param registerWithEmailUseCase Caso de uso para crear y validar nuevas cuentas por correo
 * @param loginWithGoogleUseCase Caso de uso para procesar y autorizar tokens JWT provenientes del sistema operativo
 * @param signInWithEmailUseCase Caso de uso para autenticar credenciales existentes
 * @param auth Cliente inyectado de Firebase Auth utilizado para comprobaciones de estado interno
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
     * Flujo de estado reactivo expuesto a la capa de presentación (UI)
     */
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Alterna la vista principal entre el formulario de acceso y el de creación de cuenta,
     * purgando los estados temporales de error, contraseñas y advertencias previas
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
                resetCountdown = 0,
                verificationEmailSent = false
            )
        }
    }

    /**
     * Sincroniza la entrada del campo de correo electrónico con el estado
     */
    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    /**
     * Sincroniza la entrada del campo de contraseña con el estado
     */
    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    /**
     * Sincroniza la entrada del campo de nombre de usuario con el estado
     */
    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    /**
     * Valida sintácticamente los campos requeridos antes de iniciar
     * la transacción de red y delega la validación de credenciales
     * y reglas de seguridad a los casos de uso correspondientes.
     */
    fun onRegisterClicked() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El correo y la contraseña son obligatorios.") }
            return
        }

        if (!currentState.isLoginMode && currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, introduce un nombre de usuario.") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                verificationEmailSent = false
            )
        }

        viewModelScope.launch {
            if (currentState.isLoginMode) {
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
                registerWithEmailUseCase(
                    currentState.email,
                    currentState.password,
                    currentState.username
                )
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                isLoginMode = true,
                                failedLoginAttempts = 0,
                                isPasswordResetMode = false,
                                passwordResetSent = false,
                                verificationEmailSent = true
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
     * Transiciona la interfaz de usuario al flujo dedicado de recuperación de contraseña
     */
    fun enterPasswordResetMode() {
        _uiState.update {
            it.copy(
                isPasswordResetMode = true,
                errorMessage = null,
                passwordResetSent = false,
                verificationEmailSent = false
            )
        }
    }

    /**
     * Aborta el flujo de recuperación de contraseña y restaura la vista de inicio de sesión estándar
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
     * Solicita al proveedor de autenticación el envío de un correo con enlace seguro para
     * restablecer la contraseña asociada al correo provisto en el estado
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
     * Inicia una corrutina que gestiona de manera aislada el temporizador de enfriamiento
     * para mitigar los abusos en la solicitud de correos de recuperación
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
     * Realiza una validación estructural de la cadena proporcionada bajo el estándar de direcciones de correo electrónico
     *
     * @param email Cadena a evaluar
     * @return `true` si cumple con los patrones oficiales, `false` en caso contrario
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Recibe y procesa el token JWT de identidad despachado por el servicio de Credential Manager del dispositivo
     *
     * @param idToken Cadena codificada que certifica la identidad de la cuenta de Google seleccionada
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
     * Limpia de forma manual la notificación de error visible en pantalla
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}