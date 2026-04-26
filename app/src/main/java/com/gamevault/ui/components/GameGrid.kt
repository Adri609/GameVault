package com.gamevault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamevault.domain.model.Game

@Composable
fun GameGrid(
    modifier: Modifier = Modifier,
    games: List<Game>,
    onGameClick: (Game) -> Unit = {}, // Preparado para navegar al Detalle en el futuro
    onActionClick: (Game) -> Unit = {}, // Para gestionar/eliminar el juego desde la tarjeta
    howActionButtons: Boolean = false, // No mostrar el botón si está en la bóveda
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp), // Se adapta al tamaño de la pantalla
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(games, key = { it.id }) { game ->
            GameCard(
                game = game,
                onAddClick = onActionClick,
                showActionButton = howActionButtons,
                modifier = Modifier.fillMaxWidth() // Para que ocupe toda la columna de la cuadrícula
            )
        }
    }
}