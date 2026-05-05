package com.gamevault.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.ui.components.GameGrid

/**
 * Pantalla que muestra la colección personal de juegos del usuario (La Bóveda).
 * Los datos se obtienen de la base de datos local.
 */
@Composable
fun VaultScreen(
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val savedGames by viewModel.savedGames.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Mi Bóveda",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 16.dp)
        )

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
        } else {
            GameGrid(
                games = savedGames,
                onGameClick = { game -> onNavigateToGameDetail(game.id) },
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}