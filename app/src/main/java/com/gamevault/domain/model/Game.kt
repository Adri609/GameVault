package com.gamevault.domain.model

/**
 * Modelo de dominio que representa un videojuego.
 * Se utiliza en toda la capa de UI para desacoplarla de los modelos de API o base de datos.
 */
data class Game(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val summary: String? = null,
    val steamId: String? = null
)