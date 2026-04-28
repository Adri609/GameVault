package com.gamevault.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gamevault.domain.util.Resource
import com.gamevault.ui.navigation.Routes
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    var username by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        if (state.user is Resource.Success) {
            val user = state.user.data
            username = user?.username ?: ""
            profilePictureUrl = user?.profilePictureUrl ?: ""
            bio = user?.bio ?: ""
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
                isEditing = isEditing,
                isUploading = state.isUploadingImage,
                onUsernameChange = { username = it },
                onPhotoChange = { profilePictureUrl = it },
                onBioChange = { bio = it },
                onImageClick = { if (isEditing) imageLauncher.launch("image/*") }
            )

            if (isEditing) {
                Button(
                    onClick = { viewModel.updateProfile(username, profilePictureUrl, bio) },
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
                Spacer(modifier = Modifier.height(32.dp))
                
                // Estadísticas Rápidas
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Juegos", state.totalGames.toString(), Icons.Default.Casino)
                    StatCard(Modifier.weight(1f), "Media", String.format(Locale.getDefault(), "%.1f", state.averageRating / 10), Icons.Default.Star)
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                PreferenceItem("Modo Oscuro", Icons.Default.DarkMode, true) {
                    // Implementación simplificada
                }
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
                    // Sign out handled in MainScreen or TopBar, but adding here for convenience
                }
                ActionItem("Eliminar Cuenta", Icons.Default.DeleteForever, color = MaterialTheme.colorScheme.error) {
                    viewModel.deleteAccount { navController.navigate(Routes.Register.route) }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    url: String,
    username: String,
    bio: String,
    email: String,
    isEditing: Boolean,
    isUploading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPhotoChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onImageClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(enabled = isEditing) { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White
                    )
                }
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = url,
                onValueChange = onPhotoChange,
                label = { Text("URL de imagen") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text("Biografía") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
            Text(username, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (bio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(bio, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LastAddedCard(game: com.gamevault.domain.model.Game, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp, 70.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(game.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Hace poco", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PreferenceItem(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ActionItem(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = color.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = Color.Gray)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp).fillMaxWidth()
    )
}
