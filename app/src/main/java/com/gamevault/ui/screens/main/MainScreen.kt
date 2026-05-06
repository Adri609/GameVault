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
import com.gamevault.ui.components.VaultOptionsMenu
import com.gamevault.ui.navigation.Routes
import com.gamevault.ui.screens.search.SearchScreen
import com.gamevault.ui.screens.vault.VaultScreen
import com.gamevault.ui.screens.vault.VaultViewModel
import kotlinx.coroutines.launch

private object TabIndex {
    const val HOME = 0
    const val SEARCH = 1
    const val VAULT = 2
    const val COUNT = 3
}

private fun indexToRoute(index: Int): String = when (index) {
    TabIndex.HOME -> Routes.Home.route
    TabIndex.SEARCH -> Routes.Search.route
    TabIndex.VAULT -> Routes.Vault.route
    else -> Routes.Home.route
}

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

    val pagerState = rememberPagerState(
        initialPage = TabIndex.HOME,
        pageCount = { TabIndex.COUNT }
    )

    val currentRoute = indexToRoute(pagerState.currentPage)

    val vaultViewModel: VaultViewModel = hiltViewModel()
    val savedGames by vaultViewModel.savedGames.collectAsState()

    // Estados para la Bóveda
    val isListView by vaultViewModel.isListView.collectAsState()
    val currentFilter by vaultViewModel.currentFilter.collectAsState()

    Scaffold(
        topBar = {
            GameVaultTopBar(
                profilePictureUrl = userProfile?.profilePictureUrl,
                extraActions = {
                    if (pagerState.currentPage == TabIndex.VAULT) {
                        VaultOptionsMenu(
                            games = savedGames,
                            isListView = isListView,
                            onViewToggle = { vaultViewModel.toggleView() },
                            currentFilter = currentFilter,
                            onFilterSelected = { vaultViewModel.setFilter(it) }
                        )
                    }
                },
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
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                TabIndex.HOME -> HomeScreen(onNavigateToGameDetail = onNavigateToGameDetail)
                TabIndex.SEARCH -> SearchScreen(onNavigateToGameDetail = onNavigateToGameDetail)
                TabIndex.VAULT -> VaultScreen(
                    isListView = isListView,
                    currentFilter = currentFilter,
                    onNavigateToGameDetail = onNavigateToGameDetail
                )
            }
        }
    }
}