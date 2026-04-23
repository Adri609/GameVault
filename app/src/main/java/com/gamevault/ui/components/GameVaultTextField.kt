package com.gamevault.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto personalizado basado en OutlinedTextField con estilos predefinidos.
 *
 * @param value Valor actual del texto.
 * @param onValueChange Callback que se dispara cuando el texto cambia.
 * @param label Etiqueta flotante para el campo.
 * @param modifier Modificador para ajustar el diseño.
 * @param visualTransformation Transforma visualmente el texto (ej. para contraseñas).
 * @param keyboardOptions Configuración del teclado (ej. tipo de entrada, acciones).
 * @param singleLine Si el texto debe limitarse a una sola línea.
 */
@Composable
fun GameVaultTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine
    )
}
