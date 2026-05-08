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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
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
import com.gamevault.ui.components.*
import com.gamevault.ui.navigation.Routes
import com.gamevault.ui.theme.vaultGold
import com.gamevault.utils.Resource

/**
 * Pantalla principal del Perfil de Usuario ("Dashboard Premium").
 *
 * Muestra la identidad pública del usuario (Avatar, Bio, Estado) en la cabecera.
 * Dependiendo del estado, presenta:
 * - **Modo Edición:** Formulario para modificar datos y redes sociales.
 * - **Modo Lectura:** Un panel de control (Dashboard) compuesto por tarjetas (Cards)
 *   que resumen el progreso del usuario, la barra de backlog, su "ADN Gamer",
 *   sus enlaces sociales rápidos y el último juego que añadió a la colección.
 *
 * @param navController Controlador de navegación para retroceder o ir al detalle de un juego.
 * @param viewModel ViewModel inyectado por Hilt que provee el estado y las acciones del perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lanzador para el selector de imágenes de la galería
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Estados locales para los campos del formulario de edición
    var username by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var steamUsername by remember { mutableStateOf("") }
    var twitchUsername by remember { mutableStateOf("") }
    var discordUsername by remember { mutableStateOf("") }

    // Interruptor del modo edición
    var isEditing by remember { mutableStateOf(false) }

    // Sincroniza los estados locales cuando los datos del ViewModel se cargan con éxito
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

    // Feedback visual tras guardar cambios y salida del modo edición
    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            snackbarHostState.showSnackbar("Perfil actualizado")
            viewModel.resetUpdateSuccess()
            isEditing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
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

            val userData = (state.user as? Resource.Success)?.data
            val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

            // Tarjeta de identidad
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        url = profilePictureUrl,
                        username = username,
                        bio = bio,
                        email = userData?.email ?: "",
                        status = status,
                        isEditing = isEditing,
                        isUploading = state.isUploadingImage,
                        onUsernameChange = { username = it },
                        onPhotoChange = { profilePictureUrl = it },
                        onBioChange = { bio = it },
                        onStatusChange = { status = it },
                        onImageClick = { if (isEditing) imageLauncher.launch("image/*") }
                    )

                    // La fecha de registro ahora pertenece a esta tarjeta (solo visible en modo lectura)
                    if (!isEditing) {
                        userData?.let { user ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Miembro desde ${com.gamevault.utils.formatRegistrationDate(user.registrationDate)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Contenido condicional
            if (isEditing) {
                // Modo edición: Muestra los inputs modulares para las redes sociales
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Enlaces Sociales")

                SocialTextField(
                    platform = SocialPlatform.STEAM,
                    value = steamUsername,
                    onValueChange = { steamUsername = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                SocialTextField(
                    platform = SocialPlatform.TWITCH,
                    value = twitchUsername,
                    onValueChange = { twitchUsername = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                SocialTextField(
                    platform = SocialPlatform.DISCORD,
                    value = discordUsername,
                    onValueChange = { discordUsername = it }
                )

                // Botón de guardado
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            username, profilePictureUrl, bio, status, steamUsername, twitchUsername, discordUsername
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isUpdating
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(Modifier.size(20.dp), Color.White, 2.dp)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            } else {
                // Modo lectura
                Spacer(modifier = Modifier.height(24.dp))

                // Estadísticas avanzadas
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Juegos Totales",
                            value = state.totalGames.toString(),
                            icon = Icons.Default.Casino
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Completados",
                            value = state.completedGames.toString(),
                            icon = Icons.Default.EmojiEvents,
                            iconTint = vaultGold
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Media Global",
                            value = String.format(java.util.Locale.getDefault(), "%.1f", state.averageRating / 10f),
                            icon = Icons.Default.Public
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Tu Media",
                            value = String.format(java.util.Locale.getDefault(), "%.1f", state.personalAverage),
                            icon = Icons.Default.Star,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                    }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barra de progreso del backlog
                BacklogProgressCard(
                    total = state.totalGames,
                    completed = state.completedGames,
                    playing = state.playingGames,
                    cardColor = cardColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Adn Gamer
                if (state.topGenres.isNotEmpty() || state.topPlatforms.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle("Tu ADN Gamer")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (state.topGenres.isNotEmpty()) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Top Géneros", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                        state.topGenres.forEach { Text("• $it", fontSize = 13.sp, color = Color.White) }
                                    }
                                }
                                if (state.topPlatforms.isNotEmpty()) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Top Plataformas", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                        state.topPlatforms.forEach { Text("• $it", fontSize = 13.sp, color = Color.White) }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Redes sociales
                userData?.let { user ->
                    if (user.steamUsername.isNotEmpty() || user.twitchUsername.isNotEmpty() || user.discordUsername.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SectionTitle("Redes Sociales")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (user.steamUsername.isNotEmpty()) {
                                        SocialActionIcon(SocialPlatform.STEAM, user.steamUsername)
                                    }
                                    if (user.twitchUsername.isNotEmpty()) {
                                        SocialActionIcon(SocialPlatform.TWITCH, user.twitchUsername)
                                    }
                                    if (user.discordUsername.isNotEmpty()) {
                                        SocialActionIcon(SocialPlatform.DISCORD, user.discordUsername)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Último juego añadido
                state.lastAddedGame?.let { game ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("Último añadido")
                        LastAddedCard(game) {
                            navController.navigate(Routes.GameDetail.createRoute(game.id))
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }