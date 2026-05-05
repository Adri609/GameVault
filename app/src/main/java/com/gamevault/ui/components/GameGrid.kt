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

/**
 * Rejilla vertical adaptable que muestra una lista de juegos.
 * @param games Lista de juegos a mostrar.
 * @param onGameClick Acción al pulsar en la tarjeta (navegación a detalles).
 * @param onActionClick Acción al pulsar el botón de acción (añadir/eliminar).
 * @param showActionButtons Define si se muestra el botón de acción en las tarjetas.
 * @param contentPadding Relleno interno para la rejilla.
 * @param modifier Modificador para el contenedor principal.
 */
@Composable
fun GameGrid(
    games: List<Game>,
    modifier: Modifier = Modifier,
    onGameClick: (Game) -> Unit = {}, 
    onActionClick: (Game) -> Unit = {}, 
    showActionButtons: Boolean = false, 
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp), // Ajuste automático según el ancho de pantalla
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(games, key = { it.id }) { game ->
            GameCard(
                game = game,
                onGameClick = { onGameClick(it) },
                onAddClick = onActionClick,
                showActionButton = showActionButtons,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}