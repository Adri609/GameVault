package com.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.ui.theme.vaultGold

/**
 * Fila horizontal que representa un único juego dentro de la vista de lista de la Bóveda.
 *
 * Este componente está diseñado para mostrar de un vistazo toda la información
 * relevante del juego en la colección del usuario. Renderiza la portada a la izquierda
 * y, a su derecha, una jerarquía de información que incluye:
 * - Título del juego.
 * - Icono de corazón (si el usuario lo ha marcado como favorito).
 * - Etiqueta de estado (Ej.: Jugando, Completado) coloreada según el tema.
 * - Una "píldora" unificada con las puntuaciones (Global y Personal) normalizadas sobre 10.
 *
 * @param modifier Modificador opcional para el contenedor [Card] principal.
 * @param game Objeto de dominio [Game] con toda la información necesaria para rellenar la fila.
 * @param onGameClick Callback ejecutado al hacer clic en la tarjeta del juego, normalmente para navegar al detalle.
 */
@Composable
fun GameListItem(
    modifier: Modifier = Modifier,
    game: Game,
    onGameClick: (Game) -> Unit
) {
    Card(
        onClick = { onGameClick(game) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sección Izquierda: Portada
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Portada de ${game.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Sección Derecha: Detalles del Juego
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Título y Favorito
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (game.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Juego favorito",
                            tint = Color.Red,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Etiqueta de Estado en la Bóveda
                if (game.status != GameStatus.NONE) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = game.status.displayName,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Contenedor unificado de Valoraciones (Píldora)
                if (game.rating != null || game.personalRating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {

                        // Puntuación Global (IGDB)
                        game.rating?.let { globalRating ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Global ${String.format(androidx.compose.ui.text.intl.Locale.current.platformLocale, "%.1f", globalRating / 10)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella global",
                                    tint = vaultGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // Separador Vertical (Solo si ambas notas existen)
                        if (game.rating != null && game.personalRating != null) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .width(1.dp)
                                    .height(14.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            )
                        }

                        // Puntuación Personal
                        game.personalRating?.let { personal ->
                            /* Adaptador de compatibilidad para evitar inconsistencias si el usuario
                             tenía notas guardadas con el sistema anterior (sobre 5 estrellas). */
                            val normalizedPersonal = if (personal <= 5.0 && personal > 0.0) personal * 2 else personal

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Personal ${String.format(androidx.compose.ui.text.intl.Locale.current.platformLocale, "%.1f", normalizedPersonal)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella personal",
                                    tint = vaultGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}