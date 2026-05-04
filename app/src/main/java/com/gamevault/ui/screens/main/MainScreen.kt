package com.gamevault.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.ui.components.GameVaultBottomBar
import com.gamevault.ui.components.GameVaultTopBar
import com.gamevault.ui.navigation.Routes
import com.gamevault.ui.screens.search.SearchScreen
import com.gamevault.ui.screens.vault.VaultScreen
import kotlinx.coroutines.launch

/**
 * Índices de las pestañas del [HorizontalPager].
 * Se usan para sincronizar el pager con la [GameVaultBottomBar].
 */
private object TabIndex {
    const val HOME = 0
    const val SEARCH = 1
    const val VAULT = 2
    const val COUNT = 3
}

/**
 * Convierte el índice del pager a la ruta de navegación correspondiente,
 * necesario para que [GameVaultBottomBar] ilumine la pestaña activa.
 */
private fun indexToRoute(index: Int): String = when (index) {
    TabIndex.HOME -> Routes.Home.route
    TabIndex.SEARCH -> Routes.Search.route
    TabIndex.VAULT -> Routes.Vault.route
    else -> Routes.Home.route
}

/**
 * Convierte una ruta de navegación al índice del pager correspondiente.
 */
private fun routeToIndex(route: String): Int = when (route) {
    Routes.Home.route -> TabIndex.HOME
    Routes.Search.route -> TabIndex.SEARCH
    Routes.Vault.route -> TabIndex.VAULT
    else -> TabIndex.HOME
}

/**
 * Pantalla raíz de la aplicación tras el login.
 *
 * Gestiona la navegación entre las tres pestañas principales mediante un [HorizontalPager],
 * que permite tanto el toque en la [GameVaultBottomBar] como el deslizamiento horizontal
 * con el dedo. La navegación hacia destinos globales (perfil, configuración, detalle de juego,
 * cierre de sesión) se delega al NavHost externo a través de callbacks.
 *
 * @param onSignOut Callback que cierra la sesión y navega a la pantalla de autenticación.
 * @param onNavigateToProfile Callback que abre la pantalla de perfil del usuario.
 * @param onNavigateToSettings Callback que abre la pantalla de configuración de la cuenta y app.
 * @param onNavigateToGameDetail Callback que abre el detalle de un juego dado su ID.
 * @param viewModel ViewModel que expone el perfil del usuario para la TopBar.
 */
@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()

    // Estado del pager — gestiona la página actual y la animación de deslizamiento
    val pagerState = rememberPagerState(
        initialPage = TabIndex.HOME,
        pageCount = { TabIndex.COUNT }
    )

    // Ruta activa derivada del índice actual del pager, para sincronizar la BottomBar
    val currentRoute = indexToRoute(pagerState.currentPage)

    Scaffold(
        topBar = {
            GameVaultTopBar(
                profilePictureUrl = userProfile?.profilePictureUrl,
                onProfileClick = onNavigateToProfile,
                onSettingsClick = onNavigateToSettings,
                onSignOutClick = {
                    viewModel.signOut()
                    onSignOut()
                }
            )
        },
        bottomBar = {
            GameVaultBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Al tocar un item de la BottomBar anima el pager a esa página
                    scope.launch {
                        pagerState.animateScrollToPage(routeToIndex(route))
                    }
                }
            )
        }
    ) { innerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // Precarga las páginas adyacentes para que el deslizamiento sea fluido
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                TabIndex.HOME -> HomeScreen(
                    onNavigateToGameDetail = onNavigateToGameDetail
                )
                TabIndex.SEARCH -> SearchScreen(
                    onNavigateToGameDetail = onNavigateToGameDetail
                )
                TabIndex.VAULT -> VaultScreen(
                    onNavigateToGameDetail = onNavigateToGameDetail
                )
            }
        }
    }
}