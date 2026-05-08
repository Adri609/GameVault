package com.gamevault.ui.screens.auth

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalAnimationApi::class)
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
        // --- FONDO PREMIUM ---
        Image(
            painter = painterResource(id = R.drawable.fondohumos),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Degradado oscuro para que el texto resalte siempre
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // --- CONTENIDO PRINCIPAL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // Empuja el contenido hacia arriba cuando sale el teclado
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp), // Padding global en lugar de offsets
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- LOGO ---
            Image(
                painter = painterResource(id = R.drawable.logo_pmgbueno),
                contentDescription = "Logo GameVault",
                modifier = Modifier.size(180.dp), // Tamaño moderado para que quepa todo sin pisarse
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- TÍTULOS CON TRANSICIÓN ANIMADA ---
            Text(
                text = "GAMEVAULT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            AnimatedContent(
                targetState = when {
                    uiState.isPasswordResetMode -> "Recuperar Contraseña"
                    uiState.isLoginMode -> "Bienvenido de nuevo"
                    else -> "Crea tu santuario de juegos"
                },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "TitleAnimation"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }

            // --- CONTENEDOR DEL FORMULARIO (GLASSMORPHISM) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.05f), // Cristal oscuro
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // Borde holográfico sutil
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // CAMPO: USUARIO (Aparece solo en Registro)
                    AnimatedVisibility(
                        visible = !uiState.isLoginMode && !uiState.isPasswordResetMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            GameVaultTextField(
                                value = uiState.username,
                                onValueChange = { viewModel.onUsernameChanged(it) },
                                label = "Nombre de usuario"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // CAMPO: EMAIL
                    GameVaultTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = "Correo electrónico"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // CAMPO: CONTRASEÑA (Desaparece en modo recuperación)
                    AnimatedVisibility(
                        visible = !uiState.isPasswordResetMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            GameVaultTextField(
                                value = uiState.password,
                                onValueChange = viewModel::onPasswordChanged,
                                label = "Contraseña",
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // ENLACE RECUPERAR CONTRASEÑA
                    AnimatedVisibility(visible = uiState.isLoginMode && uiState.failedLoginAttempts >= 3 && !uiState.isPasswordResetMode) {
                        TextButton(onClick = { viewModel.enterPasswordResetMode() }) {
                            Text(
                                text = "¿Has olvidado tu contraseña? Recupérala aquí",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // MENSAJES DE ESTADO (Errores / Éxito)
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (uiState.verificationEmailSent && uiState.isLoginMode && uiState.errorMessage == null) {
                        Text(
                            text = "¡Registro exitoso! Revisa tu bandeja de entrada y verifica tu cuenta para iniciar sesión.",
                            color = Color(0xFF66C0F4), // Azul vibrante
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (uiState.isPasswordResetMode && uiState.passwordResetSent) {
                        Text(
                            text = "Correo enviado. Por favor, revisa tu bandeja de entrada y la carpeta de Spam.",
                            color = Color(0xFF66C0F4),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- BOTÓN PRINCIPAL ---
                    if (uiState.isPasswordResetMode) {
                        val isCooldownActive = uiState.resetCountdown > 0
                        val buttonText = if (isCooldownActive) "Reenviar en ${uiState.resetCountdown}s" else "Enviar enlace"

                        Button(
                            onClick = { viewModel.sendPasswordReset() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !isCooldownActive && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    } else {
                        // --- BOTÓN PRINCIPAL PREMIUM ---
                        val buttonText = if (uiState.isLoginMode) "INICIAR SESIÓN" else "REGISTRARSE"

                        Button(
                            onClick = { viewModel.onRegisterClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow( // Sombra que brilla
                                    elevation = if (uiState.isLoading) 0.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = MaterialTheme.colorScheme.primary,
                                    spotColor = MaterialTheme.colorScheme.primary
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent // Transparente para que se vea el degradado
                            ),
                            contentPadding = PaddingValues() // Quitamos el padding para rellenar todo
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) // Degradado dinámico
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = uiState.isLoading,
                                    label = "ButtonAnimation"
                                ) { isLoading ->
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Text(
                                            text = buttonText,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.5.sp // Letras más separadas y elegantes
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // Fin Surface Formulario

            // --- SECCIÓN INFERIOR ---
            AnimatedVisibility(
                visible = !uiState.isPasswordResetMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                        Text(
                            text = "o continúa con",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BOTÓN  DE GOOGLE
                    Surface(
                        onClick = { scope.launch { signInWithGoogle(context, viewModel) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        enabled = !uiState.isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logogoogle), // Asegúrate de tener este icono
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Google", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TOGGLE LOGIN / REGISTRO
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleLoginMode() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isLoginMode) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (uiState.isLoginMode) "Regístrate aquí" else "Inicia sesión",
                            color = MaterialTheme.colorScheme.primary, // Color de acento
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState.isPasswordResetMode) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.exitPasswordResetMode() }) {
                        Text("Volver al inicio de sesión", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

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