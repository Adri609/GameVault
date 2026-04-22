package com.gamevault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un juego favorito almacenado en la base de datos local.
 *
 * @property id Identificador único del juego (proveniente de IGDB).
 * @property name Título del videojuego.
 * @property coverUrl Enlace a la imagen de portada.
 * @property releaseDate Fecha de lanzamiento del juego.
 */
@Entity(tableName = "favorite_games")
data class GameEntity(
    @PrimaryKey val id: Int, // ID del juego en IGDB
    val name: String, // Nombre del juego
    val coverUrl: String, // URL de la portada del juego
    val releaseDate: String // Fecha de lanzamiento del juego
)