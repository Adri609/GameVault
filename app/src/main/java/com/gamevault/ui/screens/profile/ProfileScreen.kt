package com.gamevault.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamevault.utils.Resource
import com.gamevault.ui.components.*
import com.gamevault.ui.navigation.Routes
import com.gamevault.utils.formatRegistrationDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    var username by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var steamUsername by remember { mutableStateOf("") }
    var twitchUsername by remember { mutableStateOf("") }
    var discordUsername by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        if (state.user is Resource.Success) {
            val user = state.user.data
            username = user?.username ?: ""
            profilePictureUrl = user?.profilePictureUrl ?: ""
            bio = user?.bio ?: ""
            status = user?.status ?: ""
            steamUsername = user?.steamUsername ?: ""
            twitchUsername = user?.twitchUsername ?: ""
            discordUsername = user?.discordUsername ?: ""
        }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            snackbarHostState.showSnackbar("Perfil actualizado")
            viewModel.resetUpdateSuccess()
            isEditing = false
        }
    }

    LaunchedEffect(state.passwordResetSent) {
        if (state.passwordResetSent) {
            snackbarHostState.showSnackbar("Correo de restablecimiento enviado")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) "Cancelar" else "Editar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabecera de Perfil
            ProfileHeader(
                url = profilePictureUrl,
                username = username,
                bio = bio,
                email = (state.user as? Resource.Success)?.data?.email ?: "",
                status = status,
                isEditing = isEditing,
                isUploading = state.isUploadingImage,
                onUsernameChange = { username = it },
                onPhotoChange = { profilePictureUrl = it },
                onBioChange = { bio = it },
                onStatusChange = { status = it },
                onImageClick = { if (isEditing) imageLauncher.launch("image/*") }
            )

            if (isEditing) {
                // Enlaces Sociales en edición
                SectionTitle("Enlaces Sociales")
                OutlinedTextField(
                    value = steamUsername,
                    onValueChange = { steamUsername = it },
                    label = { Text("Usuario de Steam") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.SportsEsports, null) }
                )
                OutlinedTextField(
                    value = twitchUsername,
                    onValueChange = { twitchUsername = it },
                    label = { Text("Usuario de Twitch") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LiveTv, null) }
                )
                OutlinedTextField(
                    value = discordUsername,
                    onValueChange = { discordUsername = it },
                    label = { Text("Usuario de Discord") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Chat, null) }
                )

                Button(
                    onClick = { 
                        viewModel.updateProfile(
                            username, 
                            profilePictureUrl, 
                            bio, 
                            status,
                            steamUsername,
                            twitchUsername,
                            discordUsername
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isUpdating
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Fecha de Registro
                val userData = (state.user as? Resource.Success)?.data
                userData?.let { user ->
                    Text(
                        text = "Miembro desde ${com.gamevault.utils.formatRegistrationDate(user.registrationDate)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Estadísticas Rápidas
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Juegos", state.totalGames.toString(), Icons.Default.Casino)
                    StatCard(Modifier.weight(1f), "Media", String.format(Locale.getDefault(), "%.1f", state.averageRating / 10), Icons.Default.Star)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Insights (ADN Gamer)
                if (state.topGenres.isNotEmpty() || state.topPlatforms.isNotEmpty()) {
                    SectionTitle("Tu ADN Gamer")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (state.topGenres.isNotEmpty()) {
                            Column(Modifier.weight(1f)) {
                                Text("Top Géneros", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                state.topGenres.forEach { genre ->
                                    Text("• $genre", fontSize = 13.sp)
                                }
                            }
                        }
                        if (state.topPlatforms.isNotEmpty()) {
                            Column(Modifier.weight(1f)) {
                                Text("Top Plataformas", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                state.topPlatforms.forEach { platform ->
                                    Text("• $platform", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Enlaces Sociales (Lectura)
                userData?.let { user ->
                    if (user.steamUsername.isNotEmpty() || user.twitchUsername.isNotEmpty() || user.discordUsername.isNotEmpty()) {
                        SectionTitle("Social")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (user.steamUsername.isNotEmpty()) SocialIcon(Icons.Default.SportsEsports, Color(0xFF1b2838))
                            if (user.twitchUsername.isNotEmpty()) SocialIcon(Icons.Default.LiveTv, Color(0xFF9146FF))
                            if (user.discordUsername.isNotEmpty()) SocialIcon(Icons.AutoMirrored.Filled.Chat, Color(0xFF5865F2))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Último añadido
                state.lastAddedGame?.let { game ->
                    SectionTitle("Último añadido")
                    LastAddedCard(game) {
                        navController.navigate(Routes.GameDetail.createRoute(game.id))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Preferencias
                SectionTitle("Preferencias")
                ThemeSelector(state.themeMode) { viewModel.setThemeMode(it) }
                PreferenceItem("Notificaciones", Icons.Default.Notifications, state.notificationsEnabled) {
                    viewModel.toggleNotifications(!state.notificationsEnabled)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Configuración de la cuenta
                SectionTitle("Configuración de la cuenta")
                ActionItem("Cambiar Contraseña", Icons.Default.Lock) {
                    viewModel.sendPasswordReset()
                }
                ActionItem("Cerrar Sesión", Icons.Default.Logout) {
                    viewModel.signOut()
                    navController.navigate(Routes.Register.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                ActionItem("Eliminar Cuenta", Icons.Default.DeleteForever, color = MaterialTheme.colorScheme.error) {
                    viewModel.deleteAccount { navController.navigate(Routes.Register.route) }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
