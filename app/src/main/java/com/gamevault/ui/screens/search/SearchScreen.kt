package com.gamevault.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Pantalla principal de búsqueda de videojuegos.
 *
 * Permite al usuario introducir consultas de texto para explorar el catálogo de juegos.
 * Integra un modelo reactivo para gestionar las transiciones entre los estados de red
 * (Inicial, Carga, Éxito y Error) de forma fluida.
 * * Este componente es 100% Theme-Aware, delegando la resolución de colores al `MaterialTheme.colorScheme`
 * para garantizar coherencia visual y soporte nativo al Modo Claro/Oscuro sin hardcoding de colores.
 *
 * @param onNavigateToGameDetail Callback de navegación activado al seleccionar una entidad de juego.
 * @param viewModel Puente reactivo (Hilt) gestor del estado de búsqueda y lógica de dominio.
 */
@Composable
fun SearchScreen(
    onNavigateToGameDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    // Suscripción al estado de la UI
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Delegar el color de fondo a la paleta del tema oficial (soporte nativo Claro/Oscuro)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SearchInputField(
            query = state.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = {
                viewModel.performSearch()
                // Retirar el foco del input para cerrar el teclado virtual
                focusManager.clearFocus()
            },
            placeholderText = "Buscar juegos (ej: Elden Ring)...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Crossfade(
            targetState = state.results,
            animationSpec = tween(500),
            label = "SearchTransition"
        ) { results ->
            when (results) {
                // 1. ESTADO DE CARGA
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                // ESTADO DE ERROR
                is Resource.Error -> {
                    SearchStateMessage(
                        icon = Icons.Outlined.Warning,
                        title = "Vaya, algo salió mal",
                        subtitle = results.message ?: "Ocurrió un error al buscar los juegos.",
                        iconTint = MaterialTheme.colorScheme.error
                    ) {
                        Button(
                            onClick = { viewModel.performSearch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                // ESTADO DE ÉXITO
                is Resource.Success -> {
                    val games = results.data ?: emptyList()
                    if (games.isEmpty()) {
                        // Búsqueda sin coincidencias
                        SearchStateMessage(
                            icon = Icons.Outlined.Search,
                            title = "Sin resultados",
                            subtitle = "No se encontraron juegos para \"${state.query}\"",
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Renderizado de la cuadrícula de juegos
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

                // ESTADO INICIAL
                null -> {
                    SearchStateMessage(
                        icon = Icons.Outlined.SportsEsports,
                        title = "Explora el catálogo",
                        subtitle = "Escribe el nombre de un juego para empezar a buscar.",
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Componente abstracto y reutilizable para ilustrar los estados de la máquina de red
 * (Idle, Empty, Error) mediante iconografía y tipografía estructurada.
 *
 * Utiliza los tokens semánticos de `MaterialTheme.colorScheme` de forma interna para
 * garantizar la legibilidad independientemente del tema global seleccionado.
 *
 * @param icon Recurso vectorial que representa visualmente el estado.
 * @param title Cabecera principal informativa.
 * @param subtitle Descripción de apoyo detallando el estado actual o la acción requerida.
 * @param iconTint Color de énfasis aplicado al recurso vectorial.
 * @param extraContent Slot componible para inyectar elementos de interacción (ej. Call To Actions).
 */
@Composable
fun SearchStateMessage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    extraContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .offset(y = (-40).dp)
        ) {
            // Contenedor semitransparente del icono
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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

            // Tipografía principal
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tipografía secundaria de apoyo
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Espacio de inyección para acciones adicionales
            extraContent()
        }
    }
}