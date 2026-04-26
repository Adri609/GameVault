package com.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamevault.domain.model.Game

/**
 * Componente que muestra una tarjeta con la información básica de un juego.
 * @param game Objeto [Game] con los datos a mostrar.
 * @param onAddClick Acción a ejecutar al pulsar el botón de añadir.
 * @param showActionButton Si es true, muestra un botón flotante "+" sobre la portada.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onAddClick: (Game) -> Unit = {},
    showActionButton: Boolean = true,
) {
    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            // Box para poder superponer el botón flotante a la portada
            Box {
                // Imagen de portada cargada con Coil
                AsyncImage(
                    model = game.coverUrl,
                    contentDescription = "Portada de ${game.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )

                // Botón flotante condicional (Solo se dibuja si showActionButton es true)
                if (showActionButton) {
                    IconButton(
                        onClick = { onAddClick(game) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir a la bóveda",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                // Título del juego
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                // Puntuación media (si está disponible)
                game.rating?.let {
                    Text(
                        text = "⭐ ${it.toInt() / 10}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}