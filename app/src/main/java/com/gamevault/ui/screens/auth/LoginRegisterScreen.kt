package com.gamevault.ui.screens.auth

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.gamevault.R
import com.gamevault.ui.components.GameVaultButton
import com.gamevault.ui.components.GameVaultTextField
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

/**
 * Raíz de la jerarquía de interfaces para los procesos de Identidad y Acceso
 *
 * Emplea un paradigma declarativo y dinámico para conmutar transparentemente entre
 * tres modalidades sin necesidad de incurrir en transiciones de navegación pesadas:
 * 1. **Alta de Cuenta:** Captación de credenciales primarias para nuevos usuarios
 * 2. **Inicio de Sesión:** Autenticación de usuarios preexistentes por credenciales clásicas o delegación a Google Identity
 * 3. **Gestión de Credenciales (Recuperación):** Flujo preventivo y correctivo expuesto inteligentemente
 *    tras la reincidencia de fallos de acceso
 *
 * @param viewModel Puente reactivo inyectado que aloja la lógica de dominio y los manejadores de estado
 * @param onNavigateToHome Función de orden superior ejecutada como efecto secundario una vez completado el protocolo de acceso
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondohumos),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_pmgbueno),
                contentDescription = "Logo GameVault",
                modifier = Modifier
                    .size(265.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "GAMEVAULT",
                modifier = Modifier.offset(y = (-35).dp),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.sp
                ),
                color = Color.White
            )

            Text(
                text = when {
                    uiState.isPasswordResetMode -> "Recuperar Contraseña"
                    uiState.isLoginMode -> "Bienvenido de nuevo"
                    else -> "Crea tu santuario de juegos"
                },
                modifier = Modifier.offset(y = (-35).dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .offset(y = (-15).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AnimatedVisibility(
                    visible = !uiState.isLoginMode && !uiState.isPasswordResetMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        GameVaultTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.onUsernameChanged(it) },
                            label = "Nombre de usuario"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                GameVaultTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!uiState.isPasswordResetMode) {
                    GameVaultTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = "Contraseña",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                if (uiState.isLoginMode && uiState.failedLoginAttempts >= 3 && !uiState.isPasswordResetMode) {
                    TextButton(onClick = { viewModel.enterPasswordResetMode() }) {
                        Text(
                            text = "¿Has olvidado tu contraseña? Recupérala aquí",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                if (uiState.verificationEmailSent && uiState.isLoginMode && uiState.errorMessage == null) {
                    Text(
                        text = "¡Registro exitoso! Revisa tu bandeja de entrada y verifica tu cuenta para iniciar sesión.",
                        color = Color(0xFF66C0F4),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (uiState.isPasswordResetMode && uiState.passwordResetSent) {
                    Text(
                        text = "Correo enviado. Por favor, revisa tu bandeja de entrada y la carpeta de Spam.",
                        color = Color(0xFF66C0F4),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isPasswordResetMode) {
                    val isCooldownActive = uiState.resetCountdown > 0
                    val buttonText =
                        if (isCooldownActive) "Reenviar en ${uiState.resetCountdown}s" else "Enviar enlace"

                    Button(
                        onClick = { viewModel.sendPasswordReset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isCooldownActive && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    GameVaultButton(
                        text = if (uiState.isLoginMode) "INICIAR SESIÓN" else "REGISTRARSE",
                        onClick = { viewModel.onRegisterClicked() },
                        isLoading = uiState.isLoading
                    )
                }
            }

            if (!uiState.isPasswordResetMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-5).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "o continúa con",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            signInWithGoogle(context, viewModel)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .offset(y = (-5).dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = null
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logogoogle),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                TextButton(
                    onClick = { viewModel.toggleLoginMode() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia sesión",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

            } else {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.exitPasswordResetMode() }) {
                    Text("Volver al inicio de sesión", color = Color.White)
                }
            }
        }
    }
}

/**
 * Implementación de Autenticación Federada de Google utilizando el ecosistema de Credential Manager
 *
 * Sustituye implementaciones legadas mediante la invocación de las interfaces nativas de Android, garantizando
 * una mayor privacidad y retención del usuario En caso de delegación exitosa de identidad,
 * se aísla el token JWT para delegar la autorización resolutiva en el [RegisterViewModel]
 *
 * @param context Entorno operativo requerido para el despliegue de modales del sistema
 * @param viewModel Objeto receptor y responsable de emitir las transacciones con el backend de Firebase
 */
private suspend fun signInWithGoogle(
    context: android.content.Context,
    viewModel: RegisterViewModel
) {
    try {
        val credentialManager = CredentialManager.create(context)
        val webClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(context, request)

        when (result.credential.type) {
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential =
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(
                        result.credential.data
                    )
                viewModel.onGoogleLoginTokenReceived(googleIdTokenCredential.idToken)
            }

            else -> {
                Log.e("GoogleSignIn", "Tipo de credencial no reconocido: ${result.credential.type}")
            }
        }
    } catch (e: GetCredentialException) {
        Log.e("GoogleSignIn", "Error al obtener credencial: ${e.localizedMessage}")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error inesperado durante Google Sign-In: ${e.localizedMessage}")
    }
}