package com.gamevault.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamevault.utils.Resource
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.SearchInputField
import com.gamevault.ui.navigation.Routes

/**
 * Pantalla de búsqueda que permite encontrar videojuegos por nombre y añadirlos a la bóveda.
 */
@Composable
fun SearchScreen(
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SearchInputField(
            query = state.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = {
                viewModel.performSearch()
                focusManager.clearFocus()
            },
            placeholderText = "Buscar juegos (ej: Elden Ring)...",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when (val results = state.results) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = results.message ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { viewModel.performSearch() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is Resource.Success -> {
                val games = results.data ?: emptyList()
                if (games.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No se encontraron resultados para \"${state.query}\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    GameGrid(
                        games = games,
                        onGameClick = { game -> onNavigateToGameDetail(game.id) },
                        onActionClick = { game -> viewModel.toggleVault(game) },
                        showActionButtons = true,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Escribe el nombre de un juego para empezar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
