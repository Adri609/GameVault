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
 * Pantalla de registro, inicio de sesión y restablecimiento de contraseña.
 *
 * Esta pantalla proporciona una interfaz unificada para:
 * - Registro de nuevos usuarios con email
 * - Inicio de sesión con email y contraseña
 * - Autenticación con Google usando Credential Manager (new standard)
 * - Restablecimiento de contraseña (accesible después de 3 intentos fallidos)
 *
 * La implementación utiliza AndroidX Credentials API que reemplaza el deprecated GoogleSignIn,
 * proporcionando una autenticación más segura y moderna con soporte para biometría y
 * gestión centralizada de credenciales.
 *
 * @param viewModel Instancia de [RegisterViewModel] que gestiona el estado de la pantalla
 * @param onNavigateToHome Callback ejecutado cuando la autenticación es exitosa
 *
 * @see <a href="https://developer.android.com/identity/sign-in">Android Identity Documentation</a>
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Efecto secundario para navegar cuando el proceso es exitoso
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
        // Título que cambia según el modo
        Text(
            text = if (uiState.isLoginMode) "GameVault" else "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Formulario de registro con email
        // El campo de usuario solo aparecerá en caso de estar en el formulario de registro
        if (!uiState.isLoginMode) {
            GameVaultTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameChanged(it) },
                label = "Nombre de usuario"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        GameVaultTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = "Correo electrónico"
        )

        Spacer(modifier = Modifier.height(8.dp))

        GameVaultTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = "Contraseña",
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Botón principal adaptativo
        GameVaultButton(
            text = if (uiState.isLoginMode) "Iniciar Sesión" else "Registrarse",
            onClick = { viewModel.onRegisterClicked() },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "O", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Opción de autenticación con Google usando Credential Manager
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

        // El texto "interruptor" para cambiar de modo
        TextButton(onClick = { viewModel.toggleLoginMode() }) {
            Text(
                text = if (uiState.isLoginMode)
                    "¿No tienes cuenta? Regístrate aquí"
                else
                    "¿Ya tienes cuenta? Inicia sesión"
            )
        }
    }
}

/**
 * Gestiona el flujo de autenticación con Google usando Credential Manager.
 *
 * Esta función reemplaza el deprecated GoogleSignInClient con la nueva API de Credentials,
 * que proporciona una mejor integración con Android y soporte para múltiples métodos de autenticación.
 *
 * Flujo:
 * 1. Crea una solicitud de credencial de Google ID
 * 2. Utiliza CredentialManager para manejar la autenticación
 * 3. Obtiene el token de ID de Google
 * 4. Lo pasa al ViewModel para procesarlo
 *
 * @param context Contexto de la aplicación
 * @param viewModel ViewModel que maneja la lógica de autenticación
 */
private suspend fun signInWithGoogle(
    context: android.content.Context,
    viewModel: RegisterViewModel
) {
    try {
        val credentialManager = CredentialManager.create(context)

        // Obtener el Web Client ID de los recursos
        val webClientId = context.getString(R.string.default_web_client_id)

        // Crear opciones para solicitar Google ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        // Crear la solicitud de credencial
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Ejecutar la solicitud
        val result = credentialManager.getCredential(context, request)

        // Procesar el resultado
        when (result.credential.type) {
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(result.credential.data)
                viewModel.onGoogleLoginTokenReceived(googleIdTokenCredential.idToken)
            }
            else -> {
                Log.e("GoogleSignIn", "Tipo de credencial no reconocido")
            }
        }
    } catch (e: GetCredentialException) {
        Log.e("GoogleSignIn", "Error al obtener credencial: ${e.localizedMessage}")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error inesperado: ${e.localizedMessage}")
    }
}
