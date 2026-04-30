package com.gamevault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gamevault.ui.screens.auth.RegisterScreen
import com.gamevault.ui.screens.details.GameDetailScreen
import com.gamevault.ui.screens.main.MainScreen
import com.gamevault.ui.screens.profile.ProfileScreen

/**
 * Configura el grafo de navegación de la aplicación.
 * Define los destinos y las transiciones entre las diferentes pantallas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Register.route) {

        // Pantalla de Login/Registro
        composable(Routes.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla principal (con pestañas internas)
        composable(Routes.Main.route) {
            MainScreen(
                onSignOut = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                },
                onNavigateToGameDetail = { gameId ->
                    navController.navigate(Routes.GameDetail.createRoute(gameId))
                }
            )
        }

        // Pantalla de Perfil (Global)
        composable(Routes.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // Pantalla de Detalle (Global)
        composable(
            route = Routes.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) {
            GameDetailScreen(navController = navController)
        }
    }
}
