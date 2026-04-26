package com.gamevault.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gamevault.ui.components.GameCarousel // Tu nuevo componente reutilizable

/**
 * Pantalla de inicio que muestra diferentes categorías de juegos (Populares, Lanzamientos, Esperados).
 * @param viewModel ViewModel encargado de gestionar el estado de los juegos.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val popularGames by viewModel.popularGames.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val anticipatedGames by viewModel.anticipatedGames.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Juegos Populares
        GameCarousel(
            title = "Juegos Populares",
            games = popularGames,
            onGameAddClick = { game -> viewModel.addToVault(game) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Últimos Lanzamientos
        GameCarousel(
            title = "Últimos Lanzamientos",
            games = newReleases,
            onGameAddClick = { game -> viewModel.addToVault(game) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Juegos Más Esperados
        GameCarousel(
            title = "Más Esperados",
            games = anticipatedGames,
            onGameAddClick = { game -> viewModel.addToVault(game) },
            showActionButton = false // No mostrar el botón de añadir para esta sección, solo informativa
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}