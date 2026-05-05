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
    val gradientStartColor = Color.Red
    val gradientEndColor = Color.Blue


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


    // --- DISEÑO DE LA INTERFAZ  ---
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondohumos),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Filtro oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Contenedor principal compacto (Arrangement.Top para que no se expanda innecesariamente)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Espacio inicial pequeño
            Spacer(modifier = Modifier.height(30.dp))

            // Logo ajustado a 150dp para ahorrar espacio
            Image(
                painter = painterResource(id = R.drawable.logo_pmgbueno),
                contentDescription = "Logo GameVault",
                modifier = Modifier
                    .size(265.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Textos "subidos" con offset negativo para ignorar el espacio transparente de la imagen
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
                text = if (uiState.isLoginMode) "Bienvenido de nuevo" else "Crea tu santuario de juegos",
                modifier = Modifier.offset(y = (-35).dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Formulario subido y compactado
            Column(
                modifier = Modifier
                    .offset(y = (-15).dp) // Sube todo el cuadro
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(20.dp), // Padding interno reducido
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Campo de Usuario
                AnimatedVisibility(
                    visible = !uiState.isLoginMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.onUsernameChanged(it) },
                            label = { Text("Nombre de usuario") },
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
                        Spacer(modifier = Modifier.height(8.dp)) // Espacio reducido
                    }
                }

                // Campo de Correo
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

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Contraseña
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

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Principal Degradado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
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
            } // Fin tarjeta formulario

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-5).dp), // Lo subimos un poco también
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

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Google
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // Reducido a 50dp
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

            // Enlace final
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
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
