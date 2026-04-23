package com.gamevault.ui.screens.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gamevault.R
import com.gamevault.ui.components.GameVaultButton
import com.gamevault.ui.components.GameVaultTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Pantalla de registro que permite el registro por Email y Google.
 * 
 * @param viewModel Instancia de [RegisterViewModel] para gestionar el estado.
 * @param onNavigateToHome Navegación tras una autenticación exitosa.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher para capturar el resultado del flujo de Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                viewModel.onGoogleLoginTokenReceived(token)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Error de Google Sign-In: ${e.statusCode}")
        }
    }

    // Configuración de Google Sign-In
    val webClientId = stringResource(R.string.default_web_client_id)
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

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

        // Opción de autenticación externa
        OutlinedButton(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
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