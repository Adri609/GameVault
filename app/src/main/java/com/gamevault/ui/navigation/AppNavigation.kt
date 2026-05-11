package com.gamevault.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import com.gamevault.ui.screens.settings.SettingsScreen

/**
 * Duración estándar para las animaciones de transición entre pantallas.
 */
private const val ANIM_DURATION = 300

/**
 * Configura el grafo de navegación raíz de la aplicación.
 * Define los destinos principales de nivel superior y las transiciones animadas
 * entre las diferentes pantallas utilizando Jetpack Navigation Compose.
 * * @param isDarkTheme Estado global del tema inyectado desde la MainActivity
 * para garantizar coherencia visual (Single Source of Truth) en pantallas específicas.
 */
@Composable
fun AppNavigation(isDarkTheme: Boolean) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Register.route) {

        // Register: al salir hacia Main se desliza a la izquierda
        composable(
            route = Routes.Register.route,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(tween(ANIM_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            }
        ) {
            RegisterScreen(
                isDarkTheme = isDarkTheme,
                onNavigateToHome = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Main: entra desde la derecha al venir de Register
        composable(
            route = Routes.Main.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(tween(ANIM_DURATION))
            },
            exitTransition = { fadeOut(tween(ANIM_DURATION)) },
            popEnterTransition = { fadeIn(tween(ANIM_DURATION)) }
        ) {
            MainScreen(
                onSignOut = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onNavigateToGameDetail = { gameId ->
                    navController.navigate(Routes.GameDetail.createRoute(gameId))
                }
            )
        }

        // Profile: entra deslizándose desde arriba, sale volviendo arriba
        composable(
            route = Routes.Profile.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(ANIM_DURATION)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(ANIM_DURATION)
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(ANIM_DURATION)
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(ANIM_DURATION)
                )
            }
        ) {
            ProfileScreen(navController = navController)
        }

        // Settings: entra deslizándose desde la derecha con un ligero fade
        composable(
            route = Routes.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            }
        ) {
            SettingsScreen(navController = navController)
        }

        // GameDetail: entra desde la derecha, sale hacia la derecha
        composable(
            route = Routes.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION)
                )
            }
        ) {
            GameDetailScreen(navController = navController)
        }
    }
}