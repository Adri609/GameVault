package com.gamevault.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamevault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameVaultTopBar(
    profilePictureUrl: String?,
    extraActions: @Composable RowScope.() -> Unit = {},
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var anchorPosition by remember { mutableStateOf(Offset.Zero) }

    // --- ANIMACIONES DEL AVATAR ---
    val avatarScale by animateFloatAsState(
        targetValue = if (showMenu) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "avatarScale"
    )
    val avatarGlow by animateFloatAsState(
        targetValue = if (showMenu) 1f else 0f,
        animationSpec = tween(300),
        label = "avatarGlow"
    )

    // Extraemos el color principal para usarlo en los gradientes
    val primaryColor = MaterialTheme.colorScheme.primary

    TopAppBar(
        modifier = Modifier.drawWithContent {
            drawContent()
            val strokeWidth = 1.dp.toPx()
            val y = size.height - strokeWidth / 2

            // LÍNEA DE NEÓN INFERIOR
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LOGO AJUSTADO
                Image(
                    painter = painterResource(id = R.drawable.gamevaultlogo),
                    contentDescription = "GameVault Logo",
                    modifier = Modifier.size(60.dp)
                )

                Text(
                    text = "GAMEVAULT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    ),
                    color = primaryColor
                )
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Botones dinámicos (Búsqueda, Filtros, etc.)
                extraActions()

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.padding(end = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .scale(avatarScale)
                            .onGloballyPositioned { coordinates ->
                                if (!showMenu) {
                                    anchorPosition = coordinates.positionInRoot()
                                }
                            }
                    ) {
                        // ANILLO DE AVATAR PREMIUM
                        Box(
                            modifier = Modifier
                                .size(40.dp) // Un poco más grande para darle importancia
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = (2f + avatarGlow).dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            primaryColor,
                                            primaryColor.copy(alpha = 0.3f + avatarGlow * 0.5f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp) // Espaciado entre el borde y la imagen (Estilo Instagram)
                        ) {
                            if (!profilePictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = "Perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    // MENÚ DESPLEGABLE
                    ProfileMenuOverlay(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        onProfileClick = onProfileClick,
                        onSettingsClick = onSettingsClick,
                        onSignOutClick = onSignOutClick,
                        anchorPosition = anchorPosition,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            // Fondo ligeramente translúcido (Efecto cristal)
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            titleContentColor = primaryColor,
        )
    )
}