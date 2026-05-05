package com.gamevault.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
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

@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar..."
) {
    val isDarkTheme = isSystemInDarkTheme()

    // En modo claro el fondo será un gris muy sutil para que contraste con el blanco
    val surfaceColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val iconAndPlaceholderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
    val accentColor = if (isDarkTheme) Color(0xFF03DAC6) else Color(0xFF6200EE)

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(text = placeholderText, color = iconAndPlaceholderColor)
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = iconAndPlaceholderColor)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Borrar búsqueda", tint = iconAndPlaceholderColor)
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = accentColor,
            focusedContainerColor = surfaceColor,
            unfocusedContainerColor = surfaceColor,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Red
        )
    )
}