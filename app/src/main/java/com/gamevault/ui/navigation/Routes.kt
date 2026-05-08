package com.gamevault.ui.navigation

/**
 * Define todas las rutas de navegación de la aplicación.
 *
 * Cada destino se representa como una clase object sellada. Este enfoque proporciona:
 * - **Type-safety**: El compilador verifica las rutas en tiempo de compilación
 * - **Centralizacion**: Todas las rutas en un único lugar
 * - **Mantenibilidad**: Cambios fáciles sin buscar strings dispersos
 * - **Argumentos**: Soporte seguro para parámetros de navegación
 *
 * ## Flujo de Navegación
 * ```
 * Register → Main (Vault|Search|Profile)
 *             ├→ Vault (Tab)
 *             ├→ Search (Tab)
 *             └→ Profile (Tab)
 *                  ├→ GameDetail ← Desde cualquier pantalla
 *                  └→ Settings ← Desde Profile
 * ```
 *
 * ## Tipos de Rutas
 * - **Pantalla de Autenticación**: `Register` - Punto de entrada inicial
 * - **Contenedor Principal**: `Main` - Aloja los tabs (Pager)
 * - **Pantallas de Tab**: `Home`, `Search`, `Vault` - Compartimentadas pero coordinadas
 * - **Pantallas Superpuestas**: `GameDetail`, `Profile`, `Settings` - Navegan por encima
 *
 * @see com.gamevault.ui.navigation.AppNavigation para el uso en NavHost
 */
sealed class Routes(val route: String) {
    /**
     * Ruta de **autenticación e inicialización**.
     *
     * Primera pantalla que ve el usuario. Permite:
     * - Registro con email/contraseña
     * - Inicio de sesión con Google
     * - Verificación de email requerida
     *
     * **Destino**: Main (después de login exitoso)
     */
    object Register : Routes("register_screen")

    /**
     * Ruta **contenedora principal** con Bottom Bar y Pager.
     *
     * Aloja las tres pestañas principales:
     * - Vault: Biblioteca personal
     * - Search: Búsqueda global
     * - Profile: Perfil e información
     *
     * Se muestra después del login. Es el destino raíz de la main navigation.
     */
    object Main : Routes("main_screen")

    /**
     * Ruta de la pestaña **Home** (página inicial).
     *
     * **Nota**: Esta ruta actualmente no se explora directamente en NavHost.
     * Se usa principalmente para sincronización de estado con el ViewModel.
     *
     * Puede contener:
     * - Recomendaciones personalizadas
     * - Juegos populares
     * - Noticias sobre juegos
     */
    object Home : Routes("home_screen")

    /**
     * Ruta de la pestaña **Search** (búsqueda global).
     *
     * **Nota**: Similar a Home, se usa para estado pero no como destino NavHost.
     *
     * Funcionalidades:
     * - Búsqueda en tiempo real contra IGDB
     * - Filtros por plataforma, género, rating
     * - Sugerencias automáticas
     */
    object Search : Routes("search_screen")

    /**
     * Ruta de la **Bóveda de juegos** (colección personal).
     *
     * **Nota**: Igualmente, estado coordinado con ViewModel.
     *
     * Muestra:
     * - Lista o cuadrícula de juegos personales
     * - Filtros (Por estado, favoritos, géneros)
     * - Menú de opciones avanzadas
     */
    object Vault : Routes("vault_screen")

    /**
     * Ruta del **perfil de usuario**.
     *
     * Se superpone sobre Main cuando se navega desde el tab Profile.
     *
     * Contiene:
     * - Información personal (avatar, username, bio)
     * - Estadísticas de la colección
     * - Enlace a Configuración
     * - Botón de cierre de sesión
     */
    object Profile : Routes("profile_screen")

    /**
     * Ruta de **configuración y preferencias**.
     *
     * Desde aquí el usuario puede:
     * - Cambiar tema (Claro/Oscuro/Automático)
     * - Gestionar notificaciones
     * - Cambiar contraseña
     * - Eliminar cuenta
     */
    object Settings : Routes("settings_screen")

    /**
     * Ruta de **detalles de un videojuego**.
     *
     * Se superpone sobre Main y muestra información extendida:
     * - Portada, sinopsis, géneros
     * - Rating global y personal
     * - Logros de Steam (si aplica)
     * - Botones para gestionar la bóveda
     *
     * ## Argumento Dinámico
     * - **gameId**: Long - Identificador único del juego
     *
     * @sample
     * ```kotlin
     * navController.navigate(Routes.GameDetail.createRoute(123L))
     * ```
     */
    object GameDetail : Routes("game_detail/{gameId}") {
        /**
         * Construye la ruta completa con un ID de juego específico.
         *
         * Convierte el parámetro `gameId` en una ruta válida para navegación.
         *
         * @param gameId Identificador único del videojuego (de IGDB API).
         * @return String con la ruta completa (ej: "game_detail/12345")
         *
         * @sample
         * ```kotlin
         * val route = Routes.GameDetail.createRoute(gameId = 999L)
         * navController.navigate(route)
         * ```
         */
        fun createRoute(gameId: Long) = "game_detail/$gameId"
    }
}