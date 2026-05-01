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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

@Composable
fun ProfileMenuOverlay(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onProfileClick: () -> Unit,
    onSignOutClick: () -> Unit,
    anchorPosition: Offset = Offset.Zero,
) {
    val density = LocalDensity.current

    val overlayAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseInOut),
        label = "overlayAlpha"
    )
    val menuScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "menuScale"
    )
    val menuAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = EaseInOut),
        label = "menuAlpha"
    )

    if (overlayAlpha > 0f || menuScale > 0f) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                excludeFromSystemGesture = true
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                ) {
                    // Fondo oscuro — cubre toda la pantalla
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = overlayAlpha))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDismissRequest() }
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (anchorPosition != Offset.Zero) {
                            // El IconButton estándar de M3 ocupa 48dp × 48dp
                            val iconButtonPx = with(density) { 48.dp.toPx() }
                            // Radio del recorte: ligeramente mayor que el borde del avatar (36dp/2 + margen)
                            val cutoutRadius = with(density) { 20.dp.toPx() }

                            val yOffsetCorrection = with(density) { 24.dp.toPx() }

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

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = anchorPosition.x.toInt() - 146.dp.roundToPx(),
                                y = anchorPosition.y.toInt() + 16.dp.roundToPx()
                            )
                        }
                        .graphicsLayer {
                            scaleX = menuScale
                            scaleY = menuScale
                            alpha = menuAlpha
                            transformOrigin = TransformOrigin(1f, 0f)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 0.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        MenuShimmerBar()

                        ProfileMenuItem(
                            label = "Mi Perfil",
                            icon = Icons.Default.Person,
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                onDismissRequest()
                                onProfileClick()
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        ProfileMenuItem(
                            label = "Cerrar Sesión",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = {
                                onDismissRequest()
                                onSignOutClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuShimmerBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    startX = shimmerOffset * 2500f - 1000f,
                    endX = shimmerOffset * 2500f - 400f
                )
            )
    )
}