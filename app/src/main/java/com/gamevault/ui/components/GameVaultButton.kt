package com.gamevault.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente de botón  para GameVault.
 * Incluye sombra de neón, degradado, animaciones fluidas y manejo de estados.
 *
 * @param text Texto a mostrar en el botón.
 * @param onClick Acción a ejecutar al pulsar el botón.
 * @param modifier Modificador opcional para personalizar el diseño.
 * @param isLoading Si es verdadero, muestra un indicador animado y deshabilita el clic.
 * @param enabled Si el botón está activo para interactuar.
 */
@Composable
fun GameVaultButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val isEffectivelyEnabled = enabled && !isLoading

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Un poco más alto (56dp) para mejorar la zona táctil
            .shadow(
                // Si está habilitado, sombra de neón. Si no, sombra 0.
                elevation = if (isEffectivelyEnabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Transparente para usar nuestro propio fondo
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(), // Quitamos el padding interno para que el Box ocupe todo
        enabled = isEffectivelyEnabled
    ) {
        // --- FONDO DINÁMICO ---
        val backgroundBrush = if (isEffectivelyEnabled) {
            Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            )
        } else {
            // Fondo gris translúcido cuando está desactivado
            SolidColor(Color.Gray.copy(alpha = 0.2f))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            // --- ANIMACIÓN DE CAMBIO DE ESTADO ---
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "LoadingAnimation"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = text.uppercase(),
                        color = if (isEffectivelyEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp // Letras más separadas
                    )
                }
            }
        }
    }
}