package com.gamevault.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.ui.components.GameVaultBottomBar
import com.gamevault.ui.components.GameVaultTopBar
import com.gamevault.ui.navigation.Routes
import com.gamevault.ui.screens.details.GameDetailScreen
import com.gamevault.ui.screens.profile.ProfileScreen
import com.gamevault.ui.screens.search.SearchScreen
import com.gamevault.ui.screens.vault.VaultScreen

/**
 * Pantalla principal que contiene el esqueleto de la aplicación con navegación por pestañas.
 */
@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val userProfile by viewModel.userProfile.collectAsState()

    // Obtener la ruta actual para saber dónde estamos
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lógica para ocultar la barra inferior en la pantalla de Detalles
    val showBottomBar = currentRoute in listOf(
        Routes.Home.route,
        Routes.Search.route,
        Routes.Vault.route,
        Routes.Profile.route
    )

    Scaffold(
        topBar = {
            if (showBottomBar && currentRoute != Routes.Profile.route) {
                GameVaultTopBar(
                    profilePictureUrl = userProfile?.profilePictureUrl,
                    onProfileClick = {
                        bottomNavController.navigate(Routes.Profile.route)
                    },
                    onSignOutClick = {
                        viewModel.signOut()
                        onSignOut()
                    }
                )
            }
        },
        bottomBar = {
            // Solo dibujamos la barra si estamos en las pestañas principales
            if (showBottomBar) {
                GameVaultBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        bottomNavController.navigate(route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        // Contenedor de navegación
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.Home.route) {
                HomeScreen(navController = bottomNavController)
            }

            composable(Routes.Search.route) {
                SearchScreen(navController = bottomNavController)
            }

            composable(Routes.Vault.route) {
                VaultScreen(navController = bottomNavController)
            }

            composable(Routes.Profile.route) {
                ProfileScreen(navController = bottomNavController)
            }

            composable(
                route = Routes.GameDetail.route,
                arguments = listOf(navArgument("gameId") { type = NavType.LongType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable

                GameDetailScreen(navController = bottomNavController)
            }
        }
    }
}