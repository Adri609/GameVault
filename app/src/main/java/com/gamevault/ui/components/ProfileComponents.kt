package com.gamevault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamevault.domain.model.Game
import com.gamevault.ui.theme.vaultGold
/**
 * Cabecera principal del Perfil de usuario.
 * Combina la presentación visual de la identidad con la interfaz de edición cuando es requerida.
 */
@Composable
fun ProfileHeader(
    url: String, username: String, bio: String, email: String, status: String,
    isEditing: Boolean, isUploading: Boolean,
    onUsernameChange: (String) -> Unit, onPhotoChange: (String) -> Unit,
    onBioChange: (String) -> Unit, onStatusChange: (String) -> Unit, onImageClick: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val glassBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(8.dp, CircleShape, ambientColor = accentColor, spotColor = accentColor)
                .clip(CircleShape)
                .clickable(enabled = isEditing) { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f))), CircleShape)
            )

            this@Column.AnimatedVisibility(visible = isUploading || isEditing, enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
                    if (isUploading) CircularProgressIndicator(color = accentColor)
                    else Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar", tint = Color.White)
                }
            }
        }

        // --- MODO EDICIÓN: CAMPOS MULTICOLOR ---
        AnimatedVisibility(visible = isEditing, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.padding(top = 24.dp)) {

                ColorizedTextField(
                    value = url, onValueChange = onPhotoChange, label = "URL de imagen",
                    icon = Icons.Default.Link, themeColor = Color(0xFFE91E63) // Rosa Neón
                )
                Spacer(modifier = Modifier.height(12.dp))

                ColorizedTextField(
                    value = status, onValueChange = onStatusChange, label = "Estado (ej: Jugando a...)",
                    icon = Icons.Default.SportsEsports, themeColor = Color(0xFF00E676) // Verde Gamer
                )
                Spacer(modifier = Modifier.height(12.dp))

                ColorizedTextField(
                    value = username, onValueChange = onUsernameChange, label = "Nombre",
                    icon = Icons.Default.Person, themeColor = Color(0xFF00E5FF) // Cian
                )
                Spacer(modifier = Modifier.height(12.dp))

                ColorizedTextField(
                    value = bio, onValueChange = onBioChange, label = "Biografía",
                    icon = Icons.Default.Edit, themeColor = Color(0xFFFF9800), // Naranja
                    singleLine = false, maxLines = 3
                )
            }
        }

        // --- MODO LECTURA ---
        AnimatedVisibility(visible = !isEditing, enter = fadeIn(), exit = fadeOut()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
                Text(username, fontSize = 26.sp, fontWeight = FontWeight.Black, color = textColor)
                if (status.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(50), color = accentColor.copy(alpha = 0.15f), modifier = Modifier.padding(top = 8.dp)) {
                        Text(status, fontSize = 13.sp, color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                Text(email, fontSize = 14.sp, color = secondaryColor, modifier = Modifier.padding(top = 8.dp))
                if (bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(bio, fontSize = 15.sp, textAlign = TextAlign.Center, color = secondaryColor)
                }
            }
        }
    }
}

// COMPONENTE AUXILIAR PARA LOS CAMPOS MULTICOLOR
@Composable
fun ColorizedTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    icon: ImageVector, themeColor: Color, singleLine: Boolean = true, maxLines: Int = 1
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val glassBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine, maxLines = maxLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor, unfocusedTextColor = textColor,
            focusedContainerColor = glassBg, unfocusedContainerColor = glassBg,
            focusedBorderColor = themeColor, unfocusedBorderColor = Color.Transparent,
            // Aquí le damos el color personalizado al icono y al texto superior
            focusedLeadingIconColor = themeColor, unfocusedLeadingIconColor = themeColor.copy(alpha = 0.6f),
            focusedLabelColor = themeColor, unfocusedLabelColor = themeColor.copy(alpha = 0.6f),
            cursorColor = themeColor
        )
    )
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(44.dp).background(iconTint.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BacklogProgressCard(total: Int, completed: Int, playing: Int, cardColor: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionTitle("Progreso de la Colección")

            Row(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(50)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (total == 0) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Gray.copy(alpha = 0.2f)))
                } else {
                    if (completed > 0) Box(modifier = Modifier.weight(completed.toFloat()).fillMaxHeight().background(vaultGold ?: Color(0xFFFFD700)))
                    if (playing > 0) Box(modifier = Modifier.weight(playing.toFloat()).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    val remaining = total - completed - playing
                    if (remaining > 0) Box(modifier = Modifier.weight(remaining.toFloat()).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val remaining = if (total > 0) total - completed - playing else 0
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LegendItem(color = vaultGold ?: Color(0xFFFFD700), label = "Completados", count = completed)
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Jugando", count = playing)
                LegendItem(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), label = "Pendientes", count = remaining)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label ($count)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun LastAddedCard(game: Game, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = game.coverUrl, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp, 76.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(game.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Añadido recientemente", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp).fillMaxWidth()
    )
}

@Composable
fun SocialIcon(painter: Painter, color: Color, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable { onClick() },
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = painter, contentDescription = null, modifier = Modifier.size(24.dp), tint = color)
        }
    }
}