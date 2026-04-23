package com.gamevault.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.gamevault.ui.navigation.Routes

/**
 * Define los elementos visuales de la barra de navegación inferior.
 */
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Routes.Home.route, "Inicio", Icons.Default.Home)
    object Search : BottomNavItem(Routes.Search.route, "Buscar", Icons.Default.Search)
    object Vault : BottomNavItem(Routes.Vault.route, "Bóveda", Icons.Default.Star)
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
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}