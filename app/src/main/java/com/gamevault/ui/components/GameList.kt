package com.gamevault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamevault.domain.model.Game

/**
 * Rejilla lineal vertical (Lista) que muestra los juegos en un formato expansivo.
 *
 * A diferencia de [GameGrid], este componente utiliza todo el ancho de la pantalla para
 * cada elemento, lo que permite mostrar metadatos adicionales y estadísticas del usuario
 * (como la nota personal o el estado de completado) de forma clara y legible.
 * Ideal para la vista detallada de la Bóveda del usuario.
 *
 * @param games Lista de objetos [Game] que se renderizarán en la lista.
 * @param modifier Modificador opcional para ajustar el contenedor principal (LazyColumn).
 * @param onGameClick Callback ejecutado al pulsar sobre cualquier tarjeta de la lista. Pasa el juego correspondiente.
 * @param contentPadding Relleno interno (`PaddingValues`) aplicado a la lista, útil para respetar insets como barras de navegación.
 *
 * @see GameGrid
 * @see GameListItem
 */
@Composable
fun GameList(
    games: List<Game>,
    modifier: Modifier = Modifier,
    onGameClick: (Game) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(games, key = { it.id }) { game ->
            GameListItem(
                game = game,
                onGameClick = { onGameClick(it) }
            )
        }
    }
}