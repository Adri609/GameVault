package com.gamevault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.data.local.ThemeMode

@Composable
fun ThemeSelector(currentMode: ThemeMode, onModeSelected: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "arrow_rotation")

    val currentModeText = when(currentMode) {
        ThemeMode.SYSTEM -> "Según el sistema"
        ThemeMode.DARK -> "Modo oscuro"
        ThemeMode.LIGHT -> "Modo claro"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera Clickable
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Modo Visual", fontSize = 16.sp)
                Text(currentModeText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                Modifier
                    .size(24.dp)
                    .rotate(rotationState),
                tint = Color.Gray
            )
        }

        // Contenido Expandible
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, bottom = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
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

@Composable
private fun ThemeOptionItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            Modifier.size(20.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            Modifier.weight(1f),
            fontSize = 14.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (selected) {
            Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PreferenceItem(title: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, Modifier.weight(1f), fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ActionItem(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = color.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = Color.Gray)
    }
}