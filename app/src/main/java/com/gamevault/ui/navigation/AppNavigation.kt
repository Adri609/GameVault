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
import com.gamevault.ui.screens.auth.RegisterScreen

/**
 * Configura el grafo de navegación de la aplicación.
 * Define los destinos y las transiciones entre las diferentes pantallas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Register.route) {
        composable(Routes.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    // Cuando haya éxito en el registro se navega al home
                    navController.navigate(Routes.Home.route) {
                        /*Esta línea borra el Register del historial para no volver al Register
                        * si el usuario pulsa el botón atrás del móvil*/
                        popUpTo(Routes.Register.route) { inclusive = true }
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
