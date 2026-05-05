package com.gamevault.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Representa el token de acceso que nos devuelve Twitch para poder consultar IGDB.
 */

data class TwitchToken (
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("token_type")
    val tokenType: String
)