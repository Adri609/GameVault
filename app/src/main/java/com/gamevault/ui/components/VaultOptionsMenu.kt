package com.gamevault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.ui.screens.vault.VaultFilter
import com.gamevault.ui.theme.DroppedColor
import com.gamevault.ui.theme.FavoriteColor
import com.gamevault.ui.theme.PendingColor
import com.gamevault.ui.theme.PlayingColor
import com.gamevault.ui.theme.WishlistColor
import com.gamevault.ui.theme.vaultGold

/**
 * Menú desplegable dinámico y visualmente premium para la gestión de la Bóveda.
 * * Ofrece una interfaz unificada para:
 * 1. Alternar la disposición de la vista (Lista/Cuadrícula).
 * 2. Filtrar la colección por estado (Jugando, Completado, etc.) o favoritos.
 * * Implementa animaciones de muelle (Spring), efectos de respuesta háptica y
 * transiciones de entrada escalonadas para una experiencia de usuario fluida.
 *
 * @param games Lista de juegos actual para calcular los contadores dinámicos.
 * @param isListView Estado que define si la vista actual es una lista.
 * @param onViewToggle Callback para alternar el modo de vista.
 * @param currentFilter El filtro que se encuentra aplicado actualmente.
 * @param onFilterSelected Callback disparado al seleccionar una nueva categoría de filtrado.
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
    val haptics = LocalHapticFeedback.current

    val filterOptions = remember {
        listOf(VaultFilter.All, VaultFilter.Favorites) +
                GameStatus.entries
                    .filter { it != GameStatus.NONE }
                    .map { VaultFilter.ByStatus(it) }
    }

    Box {
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                expanded = !expanded
            }
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Opciones y filtros",
                tint = if (currentFilter != VaultFilter.All) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            // Sección: Alternar Vista
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200, 50)) + expandVertically(
                    animationSpec = tween(
                        300,
                        easing = EaseOutCubic
                    )
                ),
                exit = fadeOut(tween(100)) + shrinkVertically(
                    animationSpec = tween(
                        200,
                        easing = EaseInCubic
                    )
                )
            ) {
                ViewToggleMenuItem(
                    isListView = isListView,
                    onViewToggle = {
                        onViewToggle()
                        expanded = false
                    }
                )
            }

            // Divisor animado
            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(200, 100))) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Título de sección de filtros
            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(200, 150))) {
                Text(
                    text = "FILTRAR COLECCIÓN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Lista de Filtros con efecto Cascada (Staggered)
            filterOptions.forEachIndexed { index, filter ->
                val count = when (filter) {
                    is VaultFilter.All -> games.size
                    is VaultFilter.Favorites -> games.count { it.isFavorite }
                    is VaultFilter.ByStatus -> games.count { it.status == filter.status }
                }

                val itemDelay = 200 + (index * 40L).coerceAtMost(360L)

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(250, itemDelay.toInt())) +
                            expandVertically(tween(350, itemDelay.toInt(), EaseOutCubic)),
                    exit = fadeOut(tween(100))
                ) {
                    FilterMenuItem(
                        filter = filter,
                        count = count,
                        isSelected = currentFilter == filter,
                        onFilterSelected = {
                            onFilterSelected(filter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Item de menú especializado en alternar la representación visual de la Bóveda.
 */
@Composable
private fun ViewToggleMenuItem(
    isListView: Boolean,
    onViewToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "viewIconScale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isListView) 180f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "viewIconRotation"
    )

    DropdownMenuItem(
        text = {
            Text(
                text = if (isListView) "Ver como Cuadrícula" else "Ver como Lista",
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (isListView) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .scale(iconScale)
                    .rotate(iconRotation)
            )
        },
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onViewToggle()
        },
        interactionSource = interactionSource,
        modifier = Modifier.background(
            if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
        )
    )
}

/**
 * Item de menú para categorías de filtrado. Implementa feedback háptico y efectos de "hundimiento".
 */
@Composable
private fun FilterMenuItem(
    filter: VaultFilter,
    count: Int,
    isSelected: Boolean,
    onFilterSelected: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "filterBgColor"
    )

    // Efecto Hundimiento al presionar
    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "filterItemScale"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            count == 0 && !isSelected -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "filterTextColor"
    )

    DropdownMenuItem(
        text = {
            Text(
                text = filter.title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getFilterIcon(filter),
                contentDescription = null,
                tint = getFilterTint(filter, isSelected),
                modifier = Modifier.scale(if (isSelected) 1.15f else 1f)
            )
        },
        trailingIcon = {
            FilterCountBadge(count = count, isSelected = isSelected)
        },
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onFilterSelected()
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .background(backgroundColor)
            .scale(itemScale)
    )
}

/**
 * Indicador numérico circular para cada categoría de filtro.
 */
@Composable
private fun FilterCountBadge(count: Int, isSelected: Boolean) {
    val badgeColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "badgeColor"
    )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 24.dp)
            .clip(CircleShape)
            .background(badgeColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// FUNCIONES AUXILIARES
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

@Composable
private fun getFilterTint(filter: VaultFilter, isSelected: Boolean): Color {
    if (isSelected) return MaterialTheme.colorScheme.primary
    return when (filter) {
        is VaultFilter.Favorites -> FavoriteColor
        is VaultFilter.ByStatus -> when (filter.status) {
            GameStatus.COMPLETED -> vaultGold
            GameStatus.PLAYING -> PlayingColor
            GameStatus.PENDING -> PendingColor
            GameStatus.DROPPED -> DroppedColor
            GameStatus.WISHLIST -> WishlistColor
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}