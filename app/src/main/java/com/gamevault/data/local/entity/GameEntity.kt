package com.gamevault.data.local.entity

import androidx.room.Entity

/**
 * Representa un juego favorito almacenado en la base de datos local.
 * Incluye el userId para soportar múltiples usuarios en el mismo dispositivo.
 */
@Entity(
    tableName = "favorite_games",
    primaryKeys = ["id", "userId"]
)
data class GameEntity(
    val id: Long,
    val userId: String, // ID del usuario que guardó el juego
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String>,
    val platforms: List<String>,
    val dateAdded: Long = System.currentTimeMillis(),
    val summary: String?,
    val steamId: String?,
    val isSynced: Boolean = false // Indica si se ha sincronizado con Firestore
)
