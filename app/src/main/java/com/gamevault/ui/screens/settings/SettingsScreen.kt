package com.gamevault.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamevault.ui.components.ActionItem
import com.gamevault.ui.components.PreferenceItem
import com.gamevault.ui.components.SectionTitle
import com.gamevault.ui.components.ThemeSelector
import com.gamevault.ui.navigation.Routes

/**
 * Pantalla dedicada a la gestión técnica de la aplicación.
 * Permite cambiar el tema visual, activar/desactivar notificaciones, gestionar
 * la seguridad de la cuenta y cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.passwordResetSent) {
        if (state.passwordResetSent) {
            snackbarHostState.showSnackbar("Enlace de restablecimiento enviado a tu correo")
            viewModel.resetPasswordState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

            // Preferencias
            SectionTitle("Apariencia y Sistema")
            ThemeSelector(state.themeMode) { viewModel.setThemeMode(it) }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceItem(
                "Notificaciones",
                Icons.Default.Notifications,
                state.notificationsEnabled
            ) {
                viewModel.toggleNotifications(!state.notificationsEnabled)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Configuración de la cuenta
            SectionTitle("Seguridad y Cuenta")
            ActionItem("Cambiar Contraseña", Icons.Default.Lock) {
                viewModel.sendPasswordReset()
            }
            ActionItem("Cerrar Sesión", Icons.Default.Logout) {
                viewModel.signOut()
                navController.navigate(Routes.Register.route) {
                    // Borrar el historial de navegación para que no pueda usar el botón "Atrás"
                    popUpTo(0) { inclusive = true }
                }
            }
            ActionItem(
                "Eliminar Cuenta",
                Icons.Default.DeleteForever,
                color = MaterialTheme.colorScheme.error
            ) {
                viewModel.deleteAccount {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}