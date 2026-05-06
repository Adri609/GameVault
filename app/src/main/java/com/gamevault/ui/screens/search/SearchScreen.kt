package com.gamevault.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.utils.Resource
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.SearchInputField
import com.gamevault.ui.theme.accentColor
import com.gamevault.ui.theme.backgroundColor
import com.gamevault.ui.theme.surfaceColor

/**
 * Pantalla principal de búsqueda de videojuegos.
 *
 * Permite al usuario introducir consultas de texto para buscar juegos en el catálogo,
 * gestionando de forma reactiva los diferentes estados de la petición (inicial, carga, éxito y error).
 * Además, provee accesos rápidos para añadir juegos a la bóveda personal desde los propios resultados.
 *
 * @param onNavigateToGameDetail Callback ejecutado cuando el usuario selecciona un juego; recibe el ID del mismo.
 * @param viewModel ViewModel inyectado por Hilt que contiene la lógica y el estado del flujo de búsqueda.
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
            .background(backgroundColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        when (val results = state.results) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            }

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

/**
 * Componente visual de apoyo utilizado para representar los distintos estados
 * no exitosos o informativos de la pantalla de búsqueda (estado inicial, sin resultados o error).
 *
 * @param icon Icono vectorial que ilustra el estado actual de la pantalla.
 * @param title Título principal del mensaje de estado.
 * @param subtitle Texto secundario con una explicación más detallada.
 * @param iconTint Color aplicado al icono (por defecto un blanco semitransparente).
 * @param extraContent Slot componible opcional para incluir acciones adicionales (ej. un botón de reintento).
 */
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

            extraContent()
        }
    }
}