package com.gamevault.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gamevault.ui.components.FadingBlurredBackground
import com.gamevault.ui.components.MetadataBlock
import com.gamevault.utils.formatReleaseDate
import java.util.Locale

/**
 * Pantalla de detalle de un videojuego que muestra información extendida, sinopsis y permite añadirlo a la bóveda.
 */
@Composable
fun GameDetailScreen(
    navController: NavController,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentGame = game ?: return

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleVaultState() },
                containerColor = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isSaved) "Quitar de la bóveda" else "Añadir a la bóveda"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Fondo difuminado y degradado
            FadingBlurredBackground(
                imageUrl = currentGame.coverUrl?.replace("t_cover_big", "t_1080p")
            )

            // Contenido con scroll vertical
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Botón Atrás
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }

                // Header Principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AsyncImage(
                        model = currentGame.coverUrl,
                        contentDescription = "Portada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(130.dp)
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentGame.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatReleaseDate(currentGame.releaseDate),
                            color = Color.LightGray,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        currentGame.rating?.let {
                            Text(
                                text = "⭐ ${String.format(Locale.getDefault(), "%.1f", it / 10)}/10",
                                color = Color(0xFFFFD700), // Dorado
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                MetadataBlock(title = "Géneros", items = currentGame.genres)
                MetadataBlock(title = "Plataformas", items = currentGame.platforms)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Sinopsis", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentGame.summary ?: "No hay sinopsis disponible para este juego.",
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 24.sp,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}