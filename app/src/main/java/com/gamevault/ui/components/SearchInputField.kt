package com.gamevault.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Componente reutilizable de barra de búsqueda con icono,
 * gestión de teclado y estilo Premium (Dark Theme).
 *
 * @param query Texto actual de la búsqueda.
 * @param onQueryChange Acción a ejecutar cuando el texto cambia.
 * @param onSearch Acción a ejecutar al pulsar el botón de buscar en el teclado.
 * @param placeholderText Texto de sugerencia que se muestra cuando está vacío.
 * @param modifier Modificador para el layout.
 */

@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar..."
) {
    // Colores del tema para integrar la barra perfectamente
    val accentColor = Color(0xFF03DAC6) // Cian para el cursor y borde activo
    val surfaceColor = Color.White.copy(alpha = 0.05f) // Fondo cristal / semi-transparente
    val iconAndPlaceholderColor = Color.White.copy(alpha = 0.5f) // Gris claro

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholderText,
                color = iconAndPlaceholderColor
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = iconAndPlaceholderColor
            )
        },
        // Botón (X) para limpiar el texto rápidamente
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Borrar búsqueda",
                        tint = iconAndPlaceholderColor
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        // Forma de "píldora" completamente redondeada
        shape = RoundedCornerShape(24.dp),

        // Personalización total de los colores para el Dark Theme
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = accentColor,

            // Fondo interior del campo de texto
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,

            // Comportamiento del borde (invisible por defecto, cian al pulsar)
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Red
        )
    )
}