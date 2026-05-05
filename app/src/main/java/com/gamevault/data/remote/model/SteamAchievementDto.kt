package com.gamevault.data.remote.model

import com.google.gson.annotations.SerializedName

// Capa 1: Respuesta Principal
data class SteamSchemaResponse(
    val game: SteamGame?
)

// Capa 2: Contenedor del juego
data class SteamGame(
    val gameName: String?,
    val availableGameStats: SteamAvailableStats?
)

// Capa 3: Contenedor de estadísticas
data class SteamAvailableStats(
    val achievements: List<SteamAchievementDto>? = emptyList()
)

// Capa 4: Los logros
data class SteamAchievementDto (
    val name: String,
    val displayName: String?,
    val description: String?,
    val icon: String?,
    @SerializedName("icongray") val iconGray: String?,
    val hidden: Int? // 1 si es un logro oculto para evitar spoilers
)