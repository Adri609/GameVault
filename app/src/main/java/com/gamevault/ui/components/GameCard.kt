package com.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamevault.domain.model.Game

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onGameClick: (Game) -> Unit = {},
    onAddClick: (Game) -> Unit = {},
    showActionButton: Boolean = true,
) {
    val vaultGoldAAA = Color(0xFFFFD700)

    // Usamos un Column transparente en lugar de una Card con fondo gris
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp)) // Para que el ripple táctil sea suave
            .clickable { onGameClick(game) }
    ) {

        // ==========================================
        // 1. LA PORTADA (La auténtica "Tarjeta")
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                // Sombra de neón solo en la imagen
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Imagen del juego ocupando todo el Box
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Portada de ${game.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Degradado sutil solo arriba para que el botón se vea
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)))
            )

            // Botón de añadir
            if (showActionButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        // Atrapamos el clic del botón para que no pulse la tarjeta entera
                        .clickable { onAddClick(game) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir a la bóveda",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. TEXTO FLOTANTE (Sin fondo negro)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 4.dp, end = 4.dp) // Espacio para que respire
        ) {
            // Título
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 36.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Insignia de Puntuación (Badge)
            game.rating?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(vaultGoldAAA.copy(alpha = 0.15f))
                        .border(1.dp, vaultGoldAAA.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Nota",
                        tint = vaultGoldAAA,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${it.toInt() / 10}/10",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        ),
                        color = vaultGoldAAA
                    )
                }
            } ?: Box(modifier = Modifier.height(20.dp)) // Placeholder
        }
    }
}