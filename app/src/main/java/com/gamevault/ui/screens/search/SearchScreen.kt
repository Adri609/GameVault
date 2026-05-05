package com.gamevault.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamevault.utils.Resource
import com.gamevault.ui.components.GameGrid
import com.gamevault.ui.components.SearchInputField

@Composable
fun SearchScreen(
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // --- DETECCIÓN DE MODO CLARO / OSCURO ---
    val isDarkTheme = isSystemInDarkTheme()

    // Colores
    val backgroundColor = if (isDarkTheme) Color(0xFF0D0D12) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val surfaceColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val accentColor = if (isDarkTheme) Color(0xFF03DAC6) else Color(0xFF6200EE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

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

        // --- GESTIÓN DE ESTADOS ---
        Crossfade(
            targetState = state.results,
            animationSpec = tween(500),
            label = "SearchTransition"
        ) { results ->
            when (results) {
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
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = MaterialTheme.colorScheme.error,
                        surfaceColor = surfaceColor
                    ) {
                        Button(
                            onClick = { viewModel.performSearch() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
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
                            subtitle = "No se encontraron juegos para \"${state.query}\"",
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            iconTint = secondaryTextColor,
                            surfaceColor = surfaceColor
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
                        subtitle = "Escribe el nombre de un juego para empezar a buscar.",
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        iconTint = secondaryTextColor,
                        surfaceColor = surfaceColor
                    )
                }
            }
        }
    }
}

// MENSAJES DE ESTADO
@Composable
fun SearchStateMessage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    secondaryTextColor: Color,
    iconTint: Color,
    surfaceColor: Color,
    extraContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).offset(y = (-40).dp) // Sube un poco el bloque al centro visual
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = secondaryTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            extraContent()
        }
    }
}