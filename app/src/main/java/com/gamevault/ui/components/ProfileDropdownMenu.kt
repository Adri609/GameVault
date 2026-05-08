package com.gamevault.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

/**
 * Menú emergente personalizado que se superpone a la pantalla actual.
 * Crea un efecto visual de recorte (cutout) alrededor del avatar del usuario
 * y muestra opciones de navegación como Perfil, Configuración y Cerrar sesión.
 *
 * @param expanded Determina si el menú está visible o animándose para cerrarse.
 * @param onDismissRequest Callback invocado para solicitar el cierre del menú (ej. tocar fuera).
 * @param onProfileClick Callback para navegar a la pantalla de Perfil.
 * @param onSettingsClick Callback para navegar a la pantalla de Configuración de la cuenta/app.
 * @param onSignOutClick Callback para cerrar la sesión del usuario.
 * @param anchorPosition Posición en coordenadas de pantalla del elemento ancla (el avatar) para dibujar el recorte.
 */

@Composable
fun ProfileMenuOverlay(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit, // Opcional, si tienes configuración
    onSignOutClick: () -> Unit,
    anchorPosition: Offset = Offset.Zero,
) {
    val density = LocalDensity.current

    // --- ANIMACIONES FLUIDAS ---
    val overlayAlpha by animateFloatAsState(targetValue = if (expanded) 0.6f else 0f, animationSpec = tween(300), label = "overlay")
    val menuScale by animateFloatAsState(targetValue = if (expanded) 1f else 0.8f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f), label = "scale")
    val menuAlpha by animateFloatAsState(targetValue = if (expanded) 1f else 0f, animationSpec = tween(200), label = "alpha")

    if (overlayAlpha > 0f || menuAlpha > 0f) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- FONDO OSCURO Y EFECTO FOCO DE LUZ ---
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = overlayAlpha))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismissRequest() }
                    )

                    // El recorte circular transparente
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (anchorPosition != Offset.Zero) {
                            val iconButtonPx = with(density) { 48.dp.toPx() } // Tamaño estimado del botón ancla
                            val cutoutRadius = with(density) { 22.dp.toPx() } // Radio del agujero
                            val yOffsetCorrection = with(density) { 38.dp.toPx() } // Ajuste vertical

                            drawCircle(
                                color = Color.Transparent,
                                center = Offset(
                                    x = anchorPosition.x + iconButtonPx / 2f,
                                    y = anchorPosition.y + iconButtonPx / 2f - yOffsetCorrection
                                ),
                                radius = cutoutRadius,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                }

                // --- CAJA DEL MENÚ DESPLEGABLE ---
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = anchorPosition.x.toInt() - 146.dp.roundToPx(), // Alineación a la izquierda
                                y = anchorPosition.y.toInt() + 6.dp.roundToPx()    // Espaciado hacia abajo
                            )
                        }
                        .graphicsLayer {
                            scaleX = menuScale
                            scaleY = menuScale
                            alpha = menuAlpha
                            transformOrigin = TransformOrigin(1f, 0f) // Nace de la esquina superior derecha
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = MaterialTheme.colorScheme.primary)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant) // Adaptable a claro/oscuro
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        MenuShimmerBar()

                        // OPCIONES DEL MENÚ
                        ProfileMenuItem(
                            label = "Mi Perfil",
                            icon = Icons.Default.Person,
                            onClick = { onDismissRequest(); onProfileClick() }
                        )

                        ProfileMenuItem(
                            label = "Configuración",
                            icon = Icons.Default.Settings,
                            onClick = { onDismissRequest(); onSettingsClick() }
                        )

                        // Separador
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        ProfileMenuItem(
                            label = "Cerrar Sesión",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { onDismissRequest(); onSignOutClick() }
                        )
                    }
                }
            }
        }
    }
}

// --- BARRA ANIMADA TIPO NEÓN ---
@Composable
fun MenuShimmerBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 3000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent),
                    startX = shimmerOffset * 1500f - 500f, endX = shimmerOffset * 1500f
                )
            )
    )
}