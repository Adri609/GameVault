package com.gamevault.ui.navigation

/**
 * Define las rutas de navegación de la aplicación.
 * Cada objeto representa un destino con una cadena de texto identificadora.
 */
sealed class Routes(val route: String) {
    // Pantalla de autenticación
    object Register : Routes("register_screen")

    // Pantalla principal (Contenedor con Pager y BottomBar)
    object Main : Routes("main_screen")

    // Pantallas internas de las pestañas (usadas para sincronizar estado, no en el NavHost)
    object Home : Routes("home_screen")
    object Search : Routes("search_screen")
    object Vault : Routes("vault_screen")

    // Pantallas globales superpuestas
    object Profile : Routes("profile_screen")
    object Settings : Routes("settings_screen") // Nueva ruta para Configuración

    // Pantalla de detalle con argumento dinámico
    object GameDetail : Routes("game_detail/{gameId}") {
        /**
         * Función de ayuda para construir la ruta cuando hagamos click en las tarjetas.
         * @param gameId El ID único del juego a mostrar.
         */
        fun createRoute(gameId: Long) = "game_detail/$gameId"
    }
}