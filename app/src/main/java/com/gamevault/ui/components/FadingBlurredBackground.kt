package com.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Fondo con imagen difuminada y un degradado oscuro para mejorar la legibilidad del contenido superior.
 */
@Composable
fun FadingBlurredBackground(
    modifier: Modifier = Modifier,
    imageUrl: String?
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Imágen difuminada
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
        )

        // Degradado oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(0.3f),
                            Color.Black.copy(0.95f)
                        )
                    )
                )
        )
    }
}