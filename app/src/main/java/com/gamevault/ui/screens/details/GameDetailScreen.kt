package com.gamevault.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.gamevault.domain.util.Resource
import com.gamevault.ui.components.AchievementItem
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
    val state by viewModel.state.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    when (val resource = state.game) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = resource.message ?: "Error", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.toggleHiddenAchievements() }) { // Temporal use for retry? No.
                         // Should have a retry function
                    }
                }
            }
        }
        is Resource.Success -> {
            val currentGame = resource.data ?: return
            
            // Lógica para saber si el juego es un lanzamiento futuro
            val currentTimestamp = System.currentTimeMillis() / 1000
            val isUnreleased = currentGame.releaseDate != null && currentGame.releaseDate > currentTimestamp

            Scaffold(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.toggleVaultState() },
                        containerColor = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = {
                            Icon(
                                imageVector = if (isSaved) {
                                    Icons.Default.Favorite
                                } else if (isUnreleased) {
                                    Icons.Default.DateRange
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = if (isSaved) {
                                    "Guardado"
                                } else if (isUnreleased) {
                                    "A Deseados"
                                } else {
                                    "A la Bóveda"
                                }
                            )
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                    FadingBlurredBackground(
                        imageUrl = currentGame.coverUrl?.replace("t_cover_big", "t_1080p")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(80.dp))

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
                                        color = Color(0xFFFFD700),
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

                        Spacer(modifier = Modifier.height(32.dp))

                        if (state.achievements.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Logros de Steam",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )

                                val hasHidden = state.achievements.any { it.isHidden }
                                if (hasHidden) {
                                    TextButton(
                                        onClick = { viewModel.toggleHiddenAchievements() },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (state.showHiddenAchievements) "Ocultar spoilers" else "Mostrar ocultos",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            state.achievements.forEach { achievement ->
                                AchievementItem(
                                    achievement = achievement,
                                    showHidden = state.showHiddenAchievements
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    FilledIconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 16.dp, top = 24.dp)
                            .align(Alignment.TopStart),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            }
        }
    }
}
