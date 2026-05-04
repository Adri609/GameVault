package com.gamevault.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gamevault.R

/**
 * Define las redes sociales soportadas por la aplicación.
 * Encapsula la información visual (icono, color, etiqueta) y la lógica de acción
 * específica de cada plataforma (navegar a web vs mostrar Toast).
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
    TWITCH("Usuario de Twitch", R.drawable.ic_twitch, Color(0xFF9146FF)) {
        override fun performAction(context: Context, username: String) {
            val url = "https://www.twitch.tv/$username"
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    },
    DISCORD("Usuario de Discord", R.drawable.ic_discord, Color(0xFF5865F2)) {
        override fun performAction(context: Context, username: String) {
            // Discord no tiene perfiles web públicos, así que mostramos el nombre
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
 * Campo de texto estandarizado para introducir nombres de usuario de redes sociales.
 *
 * @param platform La plataforma social que dicta el icono y la etiqueta.
 * @param value El texto actual del campo.
 * @param onValueChange Callback cuando el texto cambia.
 * @param modifier Modificador opcional.
 */
@Composable
fun SocialTextField(
    platform: SocialPlatform,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(platform.label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = platform.iconRes),
                contentDescription = platform.name,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

/**
 * Icono interactivo para redes sociales que ejecuta su propia acción al ser pulsado.
 *
 * @param platform La plataforma social a mostrar.
 * @param username El nombre de usuario vinculado a esa plataforma.
 */
@Composable
fun SocialActionIcon(
    platform: SocialPlatform,
    username: String
) {
    val context = LocalContext.current

    // Asumo que ya tienes un componente 'SocialIcon' que acepta un painter, color y onClick.
    // Si no es así, puedes usar un IconButton estándar de Material3 aquí.
    SocialIcon(
        painter = painterResource(id = platform.iconRes),
        color = platform.brandColor,
        onClick = { platform.performAction(context, username) }
    )
}