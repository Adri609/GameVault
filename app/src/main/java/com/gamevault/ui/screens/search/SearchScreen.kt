package com.gamevault.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.SearchInputField
import com.gamevault.ui.navigation.Routes

/**
 * Pantalla de búsqueda que permite encontrar videojuegos por nombre y añadirlos a la bóveda.
 */
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SearchInputField(
            query = searchQuery,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = {
                viewModel.performSearch()
                focusManager.clearFocus()
            },
            placeholderText = "Buscar juegos (ej: Elden Ring)...",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // GESTIÓN DE ESTADOS (Carga, Resultados, Vacío)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (searchResults.isNotEmpty()) {
            GameGrid(
                games = searchResults,
                onGameClick = { game ->
                    navController.navigate(Routes.GameDetail.createRoute(game.id))
                },
                onActionClick = { game -> viewModel.addToVault(game) },
                showActionButtons = true,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            )
        } else if (hasSearched && searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontraron resultados para \"$searchQuery\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}