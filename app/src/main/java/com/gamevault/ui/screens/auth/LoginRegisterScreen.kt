package com.gamevault.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.gamevault.R
import com.gamevault.ui.components.GameVaultButton
import com.gamevault.ui.components.GameVaultTextField
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

/**
 * Pantalla principal de Autenticación de GameVault.
 *
 * Gestiona de forma dinámica y reactiva tres flujos distintos sin necesidad de navegar a otras pantallas:
 * 1. **Registro:** Creación de cuenta con usuario, correo y contraseña.
 * 2. **Inicio de sesión:** Acceso con correo y contraseña, o mediante Google (Credential Manager).
 * 3. **Recuperación de contraseña:** Flujo que se activa tras 3 intentos fallidos de inicio de sesión,
 *    adaptando la interfaz para solicitar únicamente el correo electrónico e implementar un cooldown de seguridad.
 *
 * @param viewModel ViewModel inyectado por Hilt que contiene la lógica de negocio y el estado de la UI.
 * @param onNavigateToHome Callback que se ejecuta cuando el usuario se autentica con éxito.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    // Observamos el estado de forma reactiva. Cualquier cambio aquí provocará una recomposición.
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Efecto secundario: Navegar a la pantalla principal solo si la autenticación es exitosa
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // CABECERA DINÁMICA
        Text(
            text = when {
                uiState.isPasswordResetMode -> "Recuperar Contraseña"
                uiState.isLoginMode -> "GameVault"
                else -> "Crea tu cuenta"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // FORMULARIO

        // Campo de usuario: Exclusivo del modo de Registro normal
        if (!uiState.isLoginMode && !uiState.isPasswordResetMode) {
            GameVaultTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameChanged(it) },
                label = "Nombre de usuario"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Campo de correo: Visible en todos los modos
        GameVaultTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = "Correo electrónico"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de contraseña: Se oculta en el modo de recuperación
        if (!uiState.isPasswordResetMode) {
            GameVaultTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Contraseña",
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ALERTAS Y MENSAJES

        // Sugerencia de recuperación: Aparece tras 3 fallos en el login
        if (uiState.isLoginMode && uiState.failedLoginAttempts >= 3 && !uiState.isPasswordResetMode) {
            TextButton(onClick = { viewModel.enterPasswordResetMode() }) {
                Text(
                    text = "¿Has olvidado tu contraseña? Recupérala aquí",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mensaje de Error genérico devuelto por Firebase
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Aviso de éxito para la recuperación de contraseña
        if (uiState.isPasswordResetMode && uiState.passwordResetSent) {
            Text(
                text = "Correo enviado. Por favor, revisa tu bandeja de entrada y la carpeta de Spam.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // BOTÓN DE ACCIÓN PRINCIPAL

        if (uiState.isPasswordResetMode) {
            // Lógica específica para el botón de recuperación (incluye Cooldown)
            val isCooldownActive = uiState.resetCountdown > 0
            val buttonText =
                if (isCooldownActive) "Reenviar en ${uiState.resetCountdown}s" else "Enviar enlace de recuperación"

            Button(
                onClick = { viewModel.sendPasswordReset() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isCooldownActive && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(buttonText)
                }
            }
        } else {
            // Botón estándar para Iniciar Sesión o Registrarse
            GameVaultButton(
                text = if (uiState.isLoginMode) "Iniciar Sesión" else "Registrarse",
                onClick = { viewModel.onRegisterClicked() },
                isLoading = uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OPCIONES ALTERNATIVAS Y NAVEGACIÓN

        if (!uiState.isPasswordResetMode) {
            // Bloque visible solo en Login/Registro
            Text(text = "O", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Autenticación con Google
            OutlinedButton(
                onClick = {
                    scope.launch {
                        signInWithGoogle(context, viewModel)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Continuar con Google")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interruptor para alternar entre Login y Registro
            TextButton(onClick = { viewModel.toggleLoginMode() }) {
                Text(
                    text = if (uiState.isLoginMode)
                        "¿No tienes cuenta? Regístrate aquí"
                    else
                        "¿Ya tienes cuenta? Inicia sesión"
                )
            }
        } else {
            // Bloque visible solo en modo Recuperación
            Spacer(modifier = Modifier.height(16.dp))

            // Botón de escape para volver al Login estándar
            TextButton(onClick = { viewModel.exitPasswordResetMode() }) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

/**
 * Lanza el flujo de autenticación nativa de Google utilizando la API de Android Credential Manager.
 *
 * Sustituye al antiguo `GoogleSignInClient`. Este método moderno solicita al sistema operativo
 * que muestre el selector de cuentas de Google del usuario. Si el usuario selecciona una,
 * extrae el token de identidad (ID Token) y lo envía al [RegisterViewModel] para que Firebase
 * complete la autenticación.
 *
 * @param context Contexto necesario para invocar el CredentialManager y obtener los recursos.
 * @param viewModel Referencia al ViewModel para procesar el token devuelto.
 */
private suspend fun signInWithGoogle(
    context: android.content.Context,
    viewModel: RegisterViewModel
) {
    try {
        val credentialManager = CredentialManager.create(context)

        // El ID del cliente web (obtenido desde google-services.json)
        val webClientId = context.getString(R.string.default_web_client_id)

        // Configurar la petición específica para cuentas de Google
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Lanzar la UI nativa del sistema operativo para elegir cuenta
        val result = credentialManager.getCredential(context, request)

        // Verificar y procesar la respuesta
        when (result.credential.type) {
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential =
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(
                        result.credential.data
                    )
                // Pasamos el token limpio al ViewModel
                viewModel.onGoogleLoginTokenReceived(googleIdTokenCredential.idToken)
            }

            else -> {
                Log.e("GoogleSignIn", "Tipo de credencial no reconocido: ${result.credential.type}")
            }
        }
    } catch (e: GetCredentialException) {
        // El usuario canceló el diálogo u ocurrió un problema con el proveedor
        Log.e("GoogleSignIn", "Error al obtener credencial: ${e.localizedMessage}")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error inesperado durante Google Sign-In: ${e.localizedMessage}")
    }
}