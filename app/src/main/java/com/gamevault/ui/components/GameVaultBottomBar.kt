package com.gamevault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamevault.R
import com.gamevault.ui.navigation.Routes

/**
 * Define los elementos visuales de la barra de navegación inferior.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null
) {
    object Home : BottomNavItem(Routes.Home.route, "Inicio", icon = Icons.Default.Home)
    object Search : BottomNavItem(Routes.Search.route, "Buscar", icon = Icons.Default.Search)
    object Vault : BottomNavItem(Routes.Vault.route, "Bóveda", iconRes = R.drawable.ic_treasure_chest)
}

/**
 * Barra de navegación inferior de GameVault Premium (Nivel AAA).
 * Creada desde cero para evitar los estilos genéricos de Material 3 y aplicar
 * un diseño de cristal con indicadores de neón.
 */
@Composable
fun GameVaultBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Vault
    )

    // Superficie principal de la barra (Efecto Cristal oscuro)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // Translúcida
        shadowElevation = 16.dp // Sombra hacia arriba
    ) {
        // Borde superior de luz muy fina para separar la barra del contenido
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Altura especial para dar espacio a los dedos
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .navigationBarsPadding(), // Respeta los gestos del sistema en Android
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    CustomBottomNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

/**
 * Item individual de la barra animado.
 */
@Composable
private fun CustomBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // --- ANIMACIONES ---
    // El color cambia suavemente de gris al color principal
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "color"
    )

    // Efecto de rebote (escala) al ser seleccionado
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "indicator"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp)) // Para que el clic no sea un cuadrado feo
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Quitamos el efecto ripple por defecto para que sea más limpio
            ) { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ICONO CON ANIMACIÓN DE ESCALA
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            } else if (item.iconRes != null) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // TEXTO CON CAMBIO DE GROSOR Y COLOR
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = iconColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // PUNTO DE NEÓN INFERIOR
        Box(
            modifier = Modifier
                .size(4.dp)
                .graphicsLayer { alpha = indicatorAlpha }
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}