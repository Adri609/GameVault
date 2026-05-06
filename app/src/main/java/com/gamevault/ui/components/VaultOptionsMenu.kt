package com.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.ui.screens.vault.VaultFilter

/**
 * Menú desplegable dinámico y visualmente atractivo para la Bóveda.
 * Combina un interruptor de vista con filtros que incluyen iconografía dedicada
 * y contadores en forma de "badge" para una experiencia más premium.
 */
@Composable
fun VaultOptionsMenu(
    games: List<Game>,
    isListView: Boolean,
    onViewToggle: () -> Unit,
    currentFilter: VaultFilter,
    onFilterSelected: (VaultFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filterOptions = remember {
        listOf(VaultFilter.All, VaultFilter.Favorites) +
                GameStatus.entries
                    .filter { it != GameStatus.NONE }
                    .map { VaultFilter.ByStatus(it) }
    }

    Box {
        // Botón principal del menú en la TopBar
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Opciones y filtros",
                tint = if (currentFilter != VaultFilter.All) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(240.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Alternar vista
            DropdownMenuItem(
                text = { Text(if (isListView) "Ver como Cuadrícula" else "Ver como Lista") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isListView) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onViewToggle()
                    expanded = false
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // --- 2. TÍTULO DE FILTROS ---
            Text(
                text = "FILTRAR COLECCIÓN",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // --- 3. LISTA DE FILTROS ---
            filterOptions.forEach { filter ->
                val count = when (filter) {
                    is VaultFilter.All -> games.size
                    is VaultFilter.Favorites -> games.count { it.isFavorite }
                    is VaultFilter.ByStatus -> games.count { it.status == filter.status }
                }

                val isSelected = currentFilter == filter
                val itemColor =
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                DropdownMenuItem(
                    text = {
                        Text(
                            text = filter.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (count == 0 && !isSelected) MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.4f
                            )
                            else itemColor
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getFilterIcon(filter),
                            contentDescription = null,
                            tint = if (count == 0 && !isSelected) itemColor.copy(alpha = 0.4f)
                            else getFilterTint(filter, isSelected)
                        )
                    },
                    trailingIcon = {
                        // Badge (Píldora) con el contador
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    },
                    // Fondo sutil si está seleccionado
                    modifier = if (isSelected) Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.1f
                        )
                    ) else Modifier
                )
            }
        }
    }
}

// --- FUNCIONES AUXILIARES DE DISEÑO ---

/** Devuelve el icono adecuado según el filtro de la bóveda. */
private fun getFilterIcon(filter: VaultFilter): ImageVector {
    return when (filter) {
        is VaultFilter.All -> Icons.Default.AllInclusive
        is VaultFilter.Favorites -> Icons.Default.Favorite
        is VaultFilter.ByStatus -> when (filter.status) {
            GameStatus.PLAYING -> Icons.Outlined.SportsEsports
            GameStatus.COMPLETED -> Icons.Default.EmojiEvents
            GameStatus.PENDING -> Icons.Default.Schedule
            GameStatus.DROPPED -> Icons.Default.DeleteOutline
            GameStatus.WISHLIST -> Icons.Default.BookmarkBorder
            GameStatus.NONE -> Icons.Default.Circle
        }
    }
}

/** Devuelve un color especial para iconos destacados (como favoritos o completados) si están seleccionados. */
@Composable
private fun getFilterTint(filter: VaultFilter, isSelected: Boolean): Color {
    if (isSelected) return MaterialTheme.colorScheme.primary

    return when (filter) {
        is VaultFilter.Favorites -> Color(0xFFE91E63) // Rosa/Rojo para favoritos
        is VaultFilter.ByStatus -> when (filter.status) {
            GameStatus.COMPLETED -> Color(0xFFFFC107) // Dorado para completados
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}