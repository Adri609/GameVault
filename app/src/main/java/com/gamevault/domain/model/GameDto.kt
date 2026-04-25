package com.gamevault.domain.model.igdb

import com.google.gson.annotations.SerializedName

data class GameDto(
    val id: Long,
    val name: String,
    val cover: Cover?,
    val rating: Double?,
    @SerializedName("first_release_date")
    val firstReleasedDate: Long?
) {
    fun getCoverUrl(): String? {
        return cover?.imageId?.let {
            "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg"
        }
    }
}

data class Cover(
    val id: Long,
    @SerializedName("image_id")
    val imageId: String
)