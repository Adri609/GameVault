package com.gamevault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gamevault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameVaultTopBar(
    profilePictureUrl: String?,
    onProfileClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.gamevaultlogo),
                contentDescription = "GameVault Logo",
                modifier = Modifier.size(40.dp)
            )
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = { showMenu = true }) {
                    if (!profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mi Perfil") },
                        onClick = {
                            showMenu = false
                            onProfileClick()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión") },
                        onClick = {
                            showMenu = false
                            onSignOutClick()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}
