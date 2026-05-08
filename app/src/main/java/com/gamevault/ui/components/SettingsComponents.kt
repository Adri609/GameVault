package com.gamevault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.data.local.ThemeMode

/**
 * Componente auxiliar para contener iconos de ajustes dentro de una "cápsula" coloreada.
 * Esto le da profundidad visual y estructura a la lista.
 */
@Composable
fun SettingIconCapsule(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)) // Fondo translúcido
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), // Borde cristalino
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = color)
    }
}

@Composable
fun ThemeSelector(currentMode: ThemeMode, onModeSelected: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "arrow_rotation")

    val currentModeText = when(currentMode) {
        ThemeMode.SYSTEM -> "Según el sistema"
        ThemeMode.DARK -> "Modo oscuro"
        ThemeMode.LIGHT -> "Modo claro"
    }

    // Color de acento para la sección de apariencia (Púrpura)
    val appearanceAccent = Color(0xFF7F00FF)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera Clickable de la tarjeta
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { expanded = !expanded },
            color = Color.Transparent
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono encapsulado
                SettingIconCapsule(icon = Icons.Default.Palette, color = appearanceAccent)

                Spacer(modifier = Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Modo Visual", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    // El modo actual brilla en el color primario de la app (Cian/Túrquesa)
                    Text(currentModeText, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    Modifier
                        .size(24.dp)
                        .rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        // Contenido Expandible
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
        ) {
            // Contenedor "Glass" para las opciones anidadas
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, bottom = 12.dp, end = 4.dp) // Alineado con el texto
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(6.dp)
                ) {
                    ThemeOptionItem(
                        title = "Según el sistema",
                        icon = Icons.Default.SettingsSuggest,
                        selected = currentMode == ThemeMode.SYSTEM,
                        onClick = { onModeSelected(ThemeMode.SYSTEM) }
                    )
                    ThemeOptionItem(
                        title = "Modo oscuro",
                        icon = Icons.Default.DarkMode,
                        selected = currentMode == ThemeMode.DARK,
                        onClick = { onModeSelected(ThemeMode.DARK) }
                    )
                    ThemeOptionItem(
                        title = "Modo claro",
                        icon = Icons.Default.LightMode,
                        selected = currentMode == ThemeMode.LIGHT,
                        onClick = { onModeSelected(ThemeMode.LIGHT) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Si está seleccionado, brilla en cian/turquesa (primario)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Contenedor de la opción individual
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        color = if (selected) activeColor.copy(alpha = 0.08f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, activeColor.copy(alpha = 0.2f)) else null,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                Modifier.size(20.dp),
                tint = if (selected) activeColor else inactiveColor
            )
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface
            )
            if (selected) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = activeColor)
            }
        }
    }
}

@Composable
fun PreferenceItem(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // Color de acento para preferencias de notificaciones (Verde)
    val prefAccent = Color(0xFF00E676)

    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono encapsulado en verde
        SettingIconCapsule(icon = icon, color = prefAccent)

        Spacer(modifier = Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        // El switch usa el color primario de la app (Túrquesa) al activarse
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ActionItem(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    // Si no se pasa un color específico, usamos el color primario para el icono.
    // Usamos el color de acento rojo para cerrar sesión.
    val iconColor = if (color == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else color

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono encapsulado dinámico (rojo para cerrar sesión, primario para otros)
            SettingIconCapsule(icon = icon, color = iconColor)

            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp, start = 4.dp).fillMaxWidth()
    )
}