package com.gamevault.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Muestra un bloque de información (ej. Géneros o Plataformas) con un título y una lista de elementos separados por puntos.
 */
@Composable
fun MetadataBlock(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    // Si la lista está totalmente vacía, no pintar nada y salir.
    if (items.isEmpty()) return

    // Construir el texto final con el separador
    val formattedText = items.joinToString(" • ")

    // Si el texto final es solo la palabra "null" no hacer nada
    if (formattedText == "null") return

    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )
        Text(
            text = formattedText,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}