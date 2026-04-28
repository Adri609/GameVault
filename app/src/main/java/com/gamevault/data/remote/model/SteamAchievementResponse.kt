package com.gamevault.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Estructura para la respuesta de IPlayerService/GetGameAchievements/v1
 */
data class SteamAchievementResponse(
    val response: SteamAchievementList?
)

data class SteamAchievementList(
    val achievements: List<SteamAchievementV1Dto>? = emptyList()
)

data class SteamAchievementV1Dto(
    val name: String,
    @SerializedName("display_name") val displayName: String?,
    // En IPlayerService la descripción se llama display_description
    @SerializedName("display_description") val displayDescription: String?,
    // Fallback por si acaso en algún juego viene como description
    val description: String?,
    val icon: String?,
    @SerializedName("icongray") val iconGray: String?,
    val hidden: Int?
)
