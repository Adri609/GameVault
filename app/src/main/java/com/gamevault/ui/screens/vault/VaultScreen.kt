package com.gamevault.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.GameList

/**
 * Pantalla que muestra la colección personal de juegos del usuario (La Bóveda).
 * Los datos se obtienen de la base de datos local y permite alternar entre vista
 * de cuadrícula o lista, así como filtrar el contenido.
 *
 * @param isListView Determina si se renderiza la vista detallada en lista (true) o la cuadrícula (false).
 * @param currentFilter El filtro actualmente aplicado a la lista de juegos.
 * @param onNavigateToGameDetail Callback para navegar a la pantalla de detalles al pulsar un juego.
 * @param viewModel ViewModel inyectado por Hilt que proporciona la lista de juegos guardados.
 */
@Composable
fun VaultScreen(
    isListView: Boolean,
    currentFilter: VaultFilter,
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val savedGames by viewModel.savedGames.collectAsState()

    val filteredGames = remember(savedGames, currentFilter) {
        when (currentFilter) {
            is VaultFilter.All -> savedGames
            is VaultFilter.Favorites -> savedGames.filter { it.isFavorite }
            is VaultFilter.ByStatus -> savedGames.filter { it.status == currentFilter.status }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        if (savedGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tu bóveda está vacía.\n¡Añade algunos juegos!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes juegos en la categoría '${currentFilter.title}'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            if (isListView) {
                GameList(
                    games = filteredGames,
                    onGameClick = { game -> onNavigateToGameDetail(game.id) },
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                GameGrid(
                    games = filteredGames,
                    onGameClick = { game -> onNavigateToGameDetail(game.id) },
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}