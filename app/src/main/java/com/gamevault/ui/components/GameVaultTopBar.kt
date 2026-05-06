package com.gamevault.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gamevault.R
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInRoot

/**
 * Barra superior principal de la aplicación.
 *
 * Muestra el logotipo de GameVault y el avatar del usuario. Además, acepta un bloque
 * de componentes extra (como menús de filtrado) que se dibujarán justo antes de la foto de perfil.
 *
 * @param profilePictureUrl URL de la foto de perfil del usuario.
 * @param extraActions Slot componible para inyectar botones o menús dinámicos dependientes de la pantalla actual.
 * @param onProfileClick Callback invocado al seleccionar "Mi Perfil".
 * @param onSettingsClick Callback invocado al seleccionar "Configuración".
 * @param onSignOutClick Callback invocado al seleccionar "Cerrar Sesión".
 */
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

    val avatarScale by animateFloatAsState(
        targetValue = if (showMenu) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "avatarScale"
    )
    val avatarGlow by animateFloatAsState(
        targetValue = if (showMenu) 1f else 0f,
        animationSpec = tween(200),
        label = "avatarGlow"
    )

    TopAppBar(
        modifier = Modifier.drawWithContent {
            drawContent()
            val strokeWidth = 1.dp.toPx()
            val y = size.height - strokeWidth / 2
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gamevaultlogo),
                    contentDescription = "GameVault Logo",
                    modifier = Modifier.size(75.dp)
                )

                Text(
                    text = "GameVault",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp)
                )
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Renderiza los botones dinámicos inyectados
                extraActions()

                Box(
                    modifier = Modifier.padding(end = 8.dp),
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = (1.5f + avatarGlow * 1.5f).dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f + avatarGlow * 0.5f),
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                        ) {
                            if (!profilePictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Perfil",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
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
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}