package com.gamevault.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gamevault.R
import com.gamevault.ui.navigation.Routes

/**
 * Define los elementos visuales de la barra de navegación inferior.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector? = null, // Para iconos de sistema
    val iconRes: Int? = null       // Para iconos xml
) {
    object Home : BottomNavItem(Routes.Home.route, "Inicio", icon = Icons.Default.Home)
    object Search : BottomNavItem(Routes.Search.route, "Buscar", icon = Icons.Default.Search)
    object Vault :
        BottomNavItem(Routes.Vault.route, "Bóveda", iconRes = R.drawable.ic_treasure_chest)
}

/**
 * Componente reutilizable para la barra de navegación inferior de GameVault.
 * * @param currentRoute La ruta actual en la que se encuentra el usuario (para iluminar el icono).
 * @param onNavigate Callback que se ejecuta cuando el usuario pulsa una pestaña.
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

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.icon != null) {
                        // En caso de que sea un item icon
                        Icon(imageVector = item.icon, contentDescription = item.title)
                    } else if (item.iconRes != null) {
                        // Si es un icono xml
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}