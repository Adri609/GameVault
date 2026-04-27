package com.gamevault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un juego favorito almacenado en la base de datos local.
 *
 * @property id Identificador único del juego (proveniente de IGDB).
 * @property name Título del videojuego.
 * @property coverUrl Enlace a la imagen de portada.
 * @property rating Calificación del juego
 * @property releaseDate Fecha de lanzamiento del juego.
 * @property genres Géneros del videojuego
 * @property platforms Plataformas en las que está disponible el juego
 * @property dateAdded Fecha en la que se añadió el juego para ordenar la colección
 */
@Entity(tableName = "favorite_games")
data class GameEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String>,
    val platforms: List<String>,
    val dateAdded: Long = System.currentTimeMillis(),
    val summary : String?,
    val steamId: String?
)