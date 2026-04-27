package com.gamevault.ui.screens.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gamevault.R
import com.gamevault.ui.components.GameVaultButton
import com.gamevault.ui.components.GameVaultTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlin.collections.listOf
import kotlin.jvm.java

/**
 * Pantalla de registro que permite el registro por Email y Google.
 * 
 * @param viewModel Instancia de [RegisterViewModel] para gestionar el estado.
 * @param onNavigateToHome Navegación tras una autenticación exitosa.
 */
@Composable
fun RegisterScreen(

    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val gradientStartColor = Color(0xFF6200EE)
    val gradientEndColor = Color(0xFF03DAC6)


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

  /*  Column(
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
    }*/

    // --- DISEÑO DE LA INTERFAZ MEJORADA ---
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Imagen de Fondo con Superposición Oscura
        Image(
            painter = painterResource(id = R.drawable.fondohumos), // TU IMAGEN DE FONDO
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 2. Logo y Título Estilizado
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo GameVault",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "GAMEVAULT",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Text(
                text = if (uiState.isLoginMode) "Bienvenido de nuevo" else "Crea tu santuario de juegos",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(35.dp))

            // 3. Contenedor del Formulario (Tarjeta Semi-transparente)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo Usuario (Animado)
                AnimatedVisibility(
                    visible = !uiState.isLoginMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        //Color de los textField
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.onUsernameChanged(it) },
                            label = { Text("Nombre de usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Gray,
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Campo Email

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = gradientStartColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = gradientStartColor,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedLabelColor = gradientStartColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = gradientStartColor,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                // Mensaje de Error
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Principal
                val gradientStartColor = Color.Red
                val gradientEndColor = Color.Blue
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp) // Mantenemos la altura
                        .clip(RoundedCornerShape(16.dp)) // Borde redondeado
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(gradientStartColor, gradientEndColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.onRegisterClicked() },
                        modifier = Modifier.fillMaxSize(),
                        // HACEMOS EL BOTÓN TRANSPARENTE PARA QUE SE VEA EL DEGRADADO DE LA 'BOX'
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f) // Color cuando está cargando
                        ),
                        // ELIMINAMOS EL PADDING PREDETERMINADO
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isLoading, // Deshabilitar si está cargando
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        // MANEJO DEL ESTADO DE CARGA LOCALMENTE
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (uiState.isLoginMode) "INICIAR SESIÓN" else "REGISTRARSE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } // Fin Tarjeta Formulario

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Sección de Divisor y Google
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                Text(
                    text = "o continúa con",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Google Estilizado
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
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
                        painter = painterResource(id = R.drawable.logogoogle), // LOGO DE GOOGLE
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Interruptor de Modo Inferior
            TextButton(
                onClick = { viewModel.toggleLoginMode() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isLoginMode)
                        "¿No tienes cuenta? Regístrate aquí"
                    else
                        "¿Ya tienes cuenta? Inicia sesión",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
