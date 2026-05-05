package com.gamevault.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.R
import com.gamevault.utils.Resource
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.SearchInputField

/**
 * Pantalla de búsqueda mejorada con estilo Premium oscuro.
 */
@Composable
fun SearchScreen(
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // --- COLORES DEL TEMA ---
    val backgroundColor = Color(0xFF0D0D12)
    val accentColor = Color(0xFF03DAC6)
    val surfaceColor = Color(0xFF1A1A24)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp)
    ) {

        // --- BARRA DE BÚSQUEDA ---
        SearchInputField(
            query = state.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = {
                viewModel.performSearch()
                focusManager.clearFocus()
            },
            placeholderText = "Buscar juegos (ej: Elden Ring)...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // --- GESTIÓN DE BÚSQUEDA ---
        when (val results = state.results) {

            // 1. ESTADO DE CARGA
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            }

            // 2. ESTADO DE ERROR
            is Resource.Error -> {
                SearchStateMessage(
                    icon = Icons.Outlined.Warning,
                    title = "Vaya, algo salió mal",
                    subtitle = results.message ?: "Ocurrió un error al buscar los juegos.",
                    iconTint = MaterialTheme.colorScheme.error
                ) {
                    Button(
                        onClick = { viewModel.performSearch() },
                        colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = Color.White),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            // 3. ESTADO DE ÉXITO
            is Resource.Success -> {
                val games = results.data ?: emptyList()
                if (games.isEmpty()) {
                    SearchStateMessage(
                        icon = Icons.Outlined.Search,
                        title = "Sin resultados",
                        subtitle = "No se encontraron juegos para \"${state.query}\""
                    )
                } else {
                    GameGrid(
                        games = games,
                        onGameClick = { game -> onNavigateToGameDetail(game.id) },
                        onActionClick = { game -> viewModel.toggleVault(game) },
                        showActionButtons = true,
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 4. ESTADO INICIAL
            null -> {
                SearchStateMessage(
                    icon = Icons.Outlined.SportsEsports,
                    title = "Explora el catálogo",
                    subtitle = "Escribe el nombre de un juego para empezar a buscar."
                )
            }
        }
    }
}

//MENSAJES DE ERROR
@Composable
fun SearchStateMessage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = Color.White.copy(alpha = 0.5f),
    extraContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            extraContent() // Para añadir botones extra, como el de "Reintentar"
        }
    }
}