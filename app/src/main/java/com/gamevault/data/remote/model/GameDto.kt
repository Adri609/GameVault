package com.gamevault.domain.model.igdb

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos (DTO) para los juegos devueltos por la API de IGDB.
 */
data class GameDto(
    val id: Long,
    val name: String,
    val cover: Cover?,
    val rating: Double?,
    @SerializedName("first_release_date")
    val firstReleasedDate: Long?,
    val genres: List<GenreDto>?,
    val platforms: List<PlatformDto>?,
    val summary: String?,
    @SerializedName("external_games") val externalGames: List<ExternalGameDto>? = null,
    val websites: List<WebsiteDto>? = null
) {
    /**
     * Construye la URL completa de la imagen de portada usando el image_id de IGDB.
     */
    fun getCoverUrl(): String? {
        return cover?.imageId?.let {
            "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
        }
    }
}

/**
 * Modelo para los géneros en la respuesta de la API.
 */
data class GenreDto(val name: String)

/**
 * Modelo para las plataformas en la respuesta de la API.
 */
data class PlatformDto(val name: String)

/**
 * Modelo para la información de portada en la respuesta de la API.
 */
data class Cover(
    val id: Long,
    @SerializedName("image_id")
    val imageId: String
)

/**
 * Modelo para la información de los logros de Steam
 */

data class ExternalGameDto(
    val category: Int,
    val uid: String
)

data class WebsiteDto(
    val category: Int,
    val url: String?
)