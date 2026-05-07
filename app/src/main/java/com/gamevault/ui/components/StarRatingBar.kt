package com.gamevault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.ui.theme.ratingRed
import com.gamevault.ui.theme.vaultGold
import kotlin.math.roundToInt

/**
 * Componente interactivo Premium de valoración con estrellas.
 *
 * Permite al usuario tocar o deslizar el dedo de forma continua para otorgar
 * puntuaciones exactas (ej.: 6.3). Incluye feedback visual dinámico: colores que cambian
 * según la nota, físicas de escala y una burbuja flotante que sigue al dedo.
 *
 * @param rating Puntuación actual (de 0.0 a 10.0).
 * @param onRatingChanged Callback ejecutado en tiempo real al deslizar o tocar.
 * @param modifier Modificador para el contenedor.
 */
@Composable
fun StarRatingBar(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados internos para la interacción y posición
    var rowWidth by remember { mutableIntStateOf(1) }
    var isInteracting by remember { mutableStateOf(false) }
    var currentFingerX by remember { mutableFloatStateOf(0f) }

    // Animaciones de escala y color

    // Efecto de "hundimiento" al presionar
    val starScale by animateFloatAsState(
        targetValue = if (isInteracting) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "starScale"
    )

    // Color semántico según la puntuación
    val targetColor = when {
        rating == 0.0 -> Color.Gray.copy(alpha = 0.5f)
        rating < 5.0 -> ratingRed // Rojo
        rating < 7.5 -> Color(0xFFFFC107) // Ámbar
        else -> vaultGold // Dorado
    }
    val animatedStarColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "starColor"
    )

    // Animaciones de la burbuja flotante

    val tooltipAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(150),
        label = "tooltipAlpha"
    )
    val tooltipOffset by animateDpAsState(
        targetValue = if (isInteracting) (-38).dp else (-20).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tooltipOffset"
    )

    // Función auxiliar para calcular la nota según la coordenada X
    fun calculateRating(xPosition: Float): Double {
        val fraction = (xPosition / rowWidth.toFloat()).coerceIn(0f, 1f)
        val exactRating = fraction * 10.0
        return (exactRating * 10.0).roundToInt() / 10.0 // Redondeo a 1 decimal
    }

    // Contenedor principal que centra los elementos superpuestos
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {

        // Capa 1: Burbuja flotante (Sigue al dedo)
        Box(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = tooltipOffset.roundToPx()) }
                .graphicsLayer {
                    val safeWidth = if (rowWidth > 0) rowWidth.toFloat() else 1f
                    val clampedX = currentFingerX.coerceIn(0f, safeWidth)
                    translationX = clampedX - (safeWidth / 2)
                    alpha = tooltipAlpha
                }
                .background(animatedStarColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = String.format(
                    androidx.compose.ui.text.intl.Locale.current.platformLocale,
                    "%.1f",
                    rating
                ),
                color = Color(0xFF1A1A24), // Texto oscuro sobre el fondo de color
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        // Capa 2: Estrellas interactivas
        Box(
            modifier = Modifier
                .scale(starScale)
                .onGloballyPositioned { rowWidth = it.size.width }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        isInteracting = true
                        var x = down.position.x
                        currentFingerX = x
                        onRatingChanged(calculateRating(x))

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                change.consume()
                                x = change.position.x
                                currentFingerX = x
                                onRatingChanged(calculateRating(x))
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            val starSpacing = 4.dp
            val starSize = 36.dp

            // Estrellas de fondo (Grises/Vacías)
            Row(horizontalArrangement = Arrangement.spacedBy(starSpacing)) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(starSize)
                    )
                }
            }

            // Estrellas llenas y recortadas
            val fillFraction = (rating / 10.0).toFloat().coerceIn(0f, 1f)
            Row(
                horizontalArrangement = Arrangement.spacedBy(starSpacing),
                modifier = Modifier.clip(FractionalRectangleShape(fillFraction))
            ) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = animatedStarColor,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }
    }
}

/**
 * Shape personalizada que recorta un rectángulo mostrando solo un porcentaje
 * de su ancho total. Vital para rellenar las estrellas parcialmente.
 */
private class FractionalRectangleShape(private val fraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(left = 0f, top = 0f, right = size.width * fraction, bottom = size.height)
        )
    }
}