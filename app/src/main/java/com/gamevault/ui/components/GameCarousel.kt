package com.gamevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamevault.domain.model.Game

/**
 * Fila horizontal desplazable que muestra una colección de juegos bajo un título.
 */
@Composable
fun GameCarousel(
    modifier: Modifier = Modifier,
    title: String,
    games: List<Game>,
    onGameClick: (Game) -> Unit = {},
    onGameAddClick: (Game) -> Unit = {},
    showActionButton: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // Altura fija para que la UI no salte
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(games, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        onGameClick = { onGameClick(it) },
                        onAddClick = { onGameAddClick(it) },
                        showActionButton = showActionButton
                    )
                }
            }
        }
    }
}