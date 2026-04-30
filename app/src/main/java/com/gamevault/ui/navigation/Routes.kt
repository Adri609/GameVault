package com.gamevault.ui.navigation

/**
 * Define las rutas de navegación de la aplicación.
 * Cada objeto representa un destino con una cadena de texto identificadora.
 */
sealed class Routes(val route: String) {
    // Pantalla de autenticación
    object Register : Routes("register_screen")
    // Pantalla principal
    object Main : Routes("main_screen")

    // Pantallas de las pestañas
    object Home : Routes("home_screen")
    object Search : Routes("search_screen")
    object Vault : Routes("vault_screen")
    object Profile : Routes("profile_screen")

    object GameDetail : Routes("game_detail/{gameId}") {
        // Función de ayuda para construir la ruta cuando hagamos click en las tarjetas
        fun createRoute(gameId: Long) = "game_detail/$gameId"
    }
}