package com.gamevault.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.gamevault.ui.screens.auth.LoginScreen

/**
 * Configura el grafo de navegación de la aplicación.
 * Define los destinos y las transiciones entre las diferentes pantallas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    // Cuando haya éxito en el registro se navega al home
                    navController.navigate(Routes.Home.route) {
                        /*Esta línea borra el Login del historial para no volver al Login
                        * si el usuario pulsa el botón atrás del móvil*/
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Home.route) {
            // Pantalla temporal (Placeholder)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Pantalla Principal")
            }
        }
    }
}
