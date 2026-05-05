package com.gamevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamevault.domain.model.Game
import com.gamevault.utils.Resource

/**
 * Fila horizontal desplazable que muestra una colección de juegos bajo un título.
 */
@Composable
fun GameCarousel(
    modifier: Modifier = Modifier,
    title: String,
    state: Resource<List<Game>>,
    onGameClick: (Game) -> Unit = {},
    onGameAddClick: (Game) -> Unit = {},
    onRetry: () -> Unit = {},
    showActionButton: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (state) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
            is Resource.Success -> {
                val games = state.data ?: emptyList()
                if (games.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay juegos disponibles")
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
    }
}
