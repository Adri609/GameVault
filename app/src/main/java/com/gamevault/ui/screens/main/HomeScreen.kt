package com.gamevault.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamevault.ui.components.GameCarousel // Tu nuevo componente reutilizable

/**
 * Pantalla de inicio que muestra diferentes categorías de juegos (Populares, Lanzamientos, Esperados).
 * @param viewModel ViewModel encargado de gestionar el estado de los juegos.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    // Observar los flujos de estado del ViewModel
    val popularGames by viewModel.popularGames.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val anticipatedGames by viewModel.anticipatedGames.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Scroll vertical para navegar entre carruseles
            .padding(vertical = 16.dp)
    ) {
        // Sección de Juegos Populares
        GameCarousel(
            title = "Juegos Populares",
            games = popularGames
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Últimos Lanzamientos
        GameCarousel(
            title = "Últimos Lanzamientos",
            games = newReleases
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Juegos Más Esperados
        GameCarousel(
            title = "Más Esperados",
            games = anticipatedGames
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}