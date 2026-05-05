package com.gamevault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Componente interactivo y animado que muestra una barra de valoración mediante estrellas.
 *
 * @param rating El valor actual de la valoración (ej. de 1.0 a 5.0). Si es null, se muestran todas vacías.
 * @param onRatingChanged Callback que se ejecuta cuando el usuario pulsa sobre una estrella.
 * @param modifier Modificador opcional para ajustar el diseño del contenedor.
 * @param starSize Tamaño de cada icono de estrella (por defecto 40.dp).
 * @param activeColor Color de las estrellas rellenas.
 * @param inactiveColor Color de las estrellas vacías.
 */
@Composable
fun StarRatingBar(
    rating: Float?,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 40.dp,
    activeColor: Color = Color(0xFFFFD700), // Dorado
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
) {
    val currentRating = rating ?: 0f
    val haptic = LocalHapticFeedback.current // Generador de vibraciones

    Row(modifier = modifier) {
        for (i in 1..5) {
            val isSelected = i <= currentRating

            // Animación suave de color al cambiar de estado
            val tintColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 300),
                label = "starColorAnimation"
            )

            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Valorar con $i estrellas",
                tint = tintColor,
                modifier = Modifier
                    .size(starSize)
                    .padding(4.dp)
                    .clickable {
                        // Feedback táctil al pulsar
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRatingChanged(i.toFloat())
                    }
            )
        }
    }
}