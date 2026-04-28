package com.gamevault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.gamevault.ui.screens.auth.RegisterScreen
import com.gamevault.ui.screens.main.MainScreen // <-- ¡Importante importar MainScreen!

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
                    // Navegamos al esqueleto principal
                    navController.navigate(Routes.Main.route) {
                        /*Esta línea borra el Register del historial para no volver a la pantalla de registro
                        * si el usuario pulsa el botón atrás del móvil*/
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla principal
        composable(Routes.Main.route) {
            MainScreen(
                onSignOut = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}