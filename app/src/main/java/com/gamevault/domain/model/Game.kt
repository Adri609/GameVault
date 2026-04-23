package com.gamevault.domain.model

import com.google.gson.annotations.SerializedName

data class Game(
    val id: Long,
    val name: String,
    val cover: Cover?,
    val rating: Double?,
    // IGDB devuelve la fecha en formato unix timestamp
    @SerializedName("first_release_date")
    val firstReleasedDate: Long?
) {
    /**
     * Helper para obtener la URL de la portada en alta resolución.
     * Convierte el ID de la imagen en un enlace funcional.
     */

    fun getCoverUrl(): String? {
        return cover?.imageId?.let {
            "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
        }
    }
}

/**
 * Representa la portada del juego anidada dentro del JSON.
 */
data class Cover(
    val id: Long,
    @SerializedName("image_id")
    val imageId: String
)