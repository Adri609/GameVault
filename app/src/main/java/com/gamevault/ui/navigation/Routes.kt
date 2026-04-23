package com.gamevault.ui.navigation

/**
 * Define las rutas de navegación de la aplicación.
 * Cada objeto representa un destino con una cadena de texto identificadora.
 */
sealed class Routes(val route: String) {
    object Register : Routes("register_screen")
    object Home : Routes("home_screen")
}