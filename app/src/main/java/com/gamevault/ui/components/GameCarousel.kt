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
 * Carrusel horizontal que muestra una lista de juegos con un título.
 * @param title Título de la sección.
 * @param games Lista de juegos a mostrar.
 * @param modifier Modificador para el contenedor principal.
 */
@Composable
fun GameCarousel(
    title: String,
    games: List<Game>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Título de la sección
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (games.isEmpty()) {
            // Indicador de carga mientras la lista está vacía
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), 
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Lista horizontal de tarjetas de juego
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(games) { game ->
                    GameCard(game = game)
                }
            }
        }
    }
}