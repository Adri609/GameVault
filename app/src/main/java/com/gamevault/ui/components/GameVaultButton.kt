package com.gamevault.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Componente de botón personalizado para el diseño de GameVault.
 * Incluye soporte para un estado de carga con un indicador de progreso.
 *
 * @param text Texto a mostrar en el botón.
 * @param onClick Acción a ejecutar al pulsar el botón.
 * @param modifier Modificador opcional para personalizar el diseño.
 * @param isLoading Si es verdadero, muestra un indicador de carga y deshabilita el clic.
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
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        // Deshabilitar interacción si está cargando o desactivado manualmente
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            // Indicador de carga circular centrado en el botón
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text)
        }
    }
}
