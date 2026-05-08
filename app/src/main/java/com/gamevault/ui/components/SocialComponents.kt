package com.gamevault.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gamevault.R

/**
 * Define las redes sociales soportadas por la aplicación.
 * Encapsula la información visual (icono, color de marca, etiqueta) y la lógica de acción
 * específica de cada plataforma.
 */
enum class SocialPlatform(
    val label: String,
    val iconRes: Int,
    val brandColor: Color
) {
    STEAM("Usuario de Steam", R.drawable.ic_steamv2, Color(0xFF66C0F4)) {
        override fun performAction(context: Context, username: String) {
            val url = "https://steamcommunity.com/search/users/$username"
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    },
    TWITCH("Canal de Twitch", R.drawable.ic_twitch, Color(0xFF9146FF)) {
        override fun performAction(context: Context, username: String) {
            val url = "https://www.twitch.tv/$username"
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    },
    DISCORD("Usuario de Discord", R.drawable.ic_discord, Color(0xFF5865F2)) {
        override fun performAction(context: Context, username: String) {
            Toast.makeText(context, "Usuario Discord: $username", Toast.LENGTH_SHORT).show()
        }
    };

    /**
     * Ejecuta la acción correspondiente al hacer clic en el enlace social.
     * @param context Contexto necesario para lanzar Intents o Toasts.
     * @param username El nombre de usuario a buscar o mostrar.
     */
    abstract fun performAction(context: Context, username: String)
}

/**
 * Campo de texto PREMIUM para introducir nombres de usuario de redes sociales.
 * Aplica el efecto de "Glassmorphism" y se tiñe del color oficial de la red social al enfocarlo.
 */
@Composable
fun SocialTextField(
    platform: SocialPlatform,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val glassBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(platform.label, fontWeight = FontWeight.Bold) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = platform.iconRes),
                contentDescription = platform.name,
                modifier = Modifier.size(20.dp) // Un poco más grande para que el SVG luzca mejor
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp), // Esquinas redondeadas Premium
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = glassBg,
            unfocusedContainerColor = glassBg,

            // Usa el color del Enum (ej. Morado para Twitch) cuando tocas el campo
            focusedBorderColor = platform.brandColor,
            unfocusedBorderColor = Color.Transparent,
            focusedLeadingIconColor = platform.brandColor,
            unfocusedLeadingIconColor = platform.brandColor.copy(alpha = 0.6f),
            focusedLabelColor = platform.brandColor,
            unfocusedLabelColor = platform.brandColor.copy(alpha = 0.6f),
            cursorColor = platform.brandColor
        )
    )
}

/**
 * Icono circular interactivo para redes sociales que ejecuta su propia acción al ser pulsado.
 * Mantiene el estilo consistente con el resto de la interfaz.
 */
@Composable
fun SocialActionIcon(
    platform: SocialPlatform,
    username: String
) {
    val context = LocalContext.current

    SocialIcon(
        painter = painterResource(id = platform.iconRes),
        color = platform.brandColor,
        onClick = { platform.performAction(context, username) }
    )
}