package com.gamevault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamevault.domain.model.GameStatus

/**
 * Panel inferior emergente (Bottom Sheet) animado para añadir o editar un juego en la bóveda.
 *
 * @param initialStatus Estado actual del juego.
 * @param initialRating Valoración actual del usuario.
 * @param initialFavorite Estado actual de favorito.
 * @param isAlreadySaved Indica si el juego ya existe en la bóveda del usuario.
 * @param onDismissRequest Callback para cerrar el panel.
 * @param onSave Callback ejecutado al guardar los cambios.
 * @param onRemove Callback ejecutado cuando el usuario decide eliminar el juego de la bóveda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageVaultBottomSheet(
    initialStatus: GameStatus = GameStatus.NONE,
    initialRating: Float? = null,
    initialFavorite: Boolean = false,
    isAlreadySaved: Boolean = false,
    onDismissRequest: () -> Unit,
    onSave: (status: GameStatus, rating: Float?, isFavorite: Boolean) -> Unit,
    onRemove: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var currentStatus by remember { mutableStateOf(initialStatus) }
    var currentRating by remember { mutableStateOf(initialRating) }
    var isFavorite by remember { mutableStateOf(initialFavorite) }

    // Animaciones para el icono del corazón (Color y Efecto Rebote)
    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "heartColor"
    )
    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f, // Crece un 20% al ser favorito
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy, // Efecto muelle elástico
            stiffness = Spring.StiffnessMedium
        ),
        label = "heartScale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gestionar Bóveda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Estado
            Text(
                text = "¿En qué estado se encuentra?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameStatus.entries.toTypedArray()) { status ->
                    FilterChip(
                        selected = currentStatus == status,
                        onClick = {
                            if (currentStatus != status) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentStatus = status
                            }
                        },
                        label = { Text(status.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tu valoración",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarRatingBar(
                        rating = currentRating?.toDouble() ?: 0.0,
                        onRatingChanged = { currentRating = it.toFloat() }
                    )
                }

                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.size(56.dp) // Un poco más grande para facilitar el toque
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Marcar como favorito",
                        tint = heartColor,
                        modifier = Modifier
                            .size(36.dp)
                            .scale(heartScale) // Animación de rebote
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Guardar
            GameVaultButton(
                text = if (isAlreadySaved) "Guardar cambios" else "Guardar en mi Bóveda",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(currentStatus, currentRating, isFavorite)
                }
            )

            // Botón de Eliminar (Solo visible si ya estaba guardado)
            if (isAlreadySaved) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemove()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Eliminar de la Bóveda",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}