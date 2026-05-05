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
    onNavigateToHome: () -> Unit,
) {
    // Observar el estado de forma reactiva. Cualquier cambio aquí provocará una recomposición.
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Efecto secundario: Navegar a la pantalla principal solo si la autenticación es exitosa
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    // DISEÑO DE LA INTERFAZ
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo visual de humos
        Image(
            painter = painterResource(id = R.drawable.fondohumos),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Filtro oscuro para mejorar el contraste
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Contenedor principal compacto
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

            // Logo ajustado
            Image(
                painter = painterResource(id = R.drawable.logo_pmgbueno),
                contentDescription = "Logo GameVault",
                modifier = Modifier
                    .size(265.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Textos de cabecera con offset negativo
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

            // Tarjeta central del Formulario (Efecto Glassmorphism)
            Column(
                modifier = Modifier
                    .offset(y = (-15).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LÓGICA DEL FORMULARIO

                // 1. Campo de Usuario (Solo en Registro)
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

                // 2. Campo de Correo (Siempre visible)
                GameVaultTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 3. Campo de Contraseña (Oculto en recuperación)
                if (!uiState.isPasswordResetMode) {
                    GameVaultTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = "Contraseña",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                // ALERTAS Y MENSAJES DE ESTADO
                
                // Sugerencia de recuperación tras 3 fallos
                if (uiState.isLoginMode && uiState.failedLoginAttempts >= 3 && !uiState.isPasswordResetMode) {
                    TextButton(onClick = { viewModel.enterPasswordResetMode() }) {
                        Text(
                            text = "¿Has olvidado tu contraseña? Recupérala aquí",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mensaje de Error genérico
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Aviso de éxito de recuperación
                if (uiState.isPasswordResetMode && uiState.passwordResetSent) {
                    Text(
                        text = "Correo enviado. Por favor, revisa tu bandeja de entrada y la carpeta de Spam.",
                        color = Color(0xFF66C0F4), // Azul clarito visible en fondo oscuro
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN PRINCIPAL (Dinámico)
                if (uiState.isPasswordResetMode) {
                    val isCooldownActive = uiState.resetCountdown > 0
                    val buttonText = if (isCooldownActive) "Reenviar en ${uiState.resetCountdown}s" else "Enviar enlace"

                    Button(
                        onClick = { viewModel.sendPasswordReset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isCooldownActive && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
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
            } // Fin tarjeta formulario

            //  OPCIONES ALTERNATIVAS Y NAVEGACIÓN
            
            if (!uiState.isPasswordResetMode) {
                // Separador visual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-5).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                    Text(
                        text = "o continúa con",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de Google (Lógica moderna de main con diseño de feat)
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

                // Enlace para alternar Login/Registro
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
                // Botón de escape para modo Recuperación
                TextButton(onClick = { viewModel.exitPasswordResetMode() }) {
                    Text("Volver al inicio de sesión", color = Color.White)
                }
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
                // Pasar el token limpio al ViewModel
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