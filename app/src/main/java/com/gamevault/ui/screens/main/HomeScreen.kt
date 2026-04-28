package com.gamevault.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamevault.ui.components.GameCarousel
import com.gamevault.ui.navigation.Routes

/**
 * Pantalla de inicio que muestra diferentes categorías de juegos (Populares, Lanzamientos, Esperados).
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Juegos Populares
        GameCarousel(
            title = "Juegos Populares",
            state = state.popularGames,
            onGameClick = { game ->
                navController.navigate(Routes.GameDetail.createRoute(game.id))
            },
            onGameAddClick = { game -> viewModel.toggleVault(game) },
            onRetry = { viewModel.fetchAllCategories() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Últimos Lanzamientos
        GameCarousel(
            title = "Últimos Lanzamientos",
            state = state.newReleases,
            onGameClick = { game ->
                navController.navigate(Routes.GameDetail.createRoute(game.id))
            },
            onGameAddClick = { game -> viewModel.toggleVault(game) },
            onRetry = { viewModel.fetchAllCategories() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Juegos Más Esperados
        GameCarousel(
            title = "Más Esperados",
            state = state.anticipatedGames,
            onGameClick = { game ->
                navController.navigate(Routes.GameDetail.createRoute(game.id))
            },
            onGameAddClick = { game -> viewModel.toggleVault(game) },
            onRetry = { viewModel.fetchAllCategories() },
            showActionButton = false
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
