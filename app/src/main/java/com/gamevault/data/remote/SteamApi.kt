package com.gamevault.data.remote

import com.gamevault.data.remote.model.SteamAchievementResponse
import com.gamevault.data.remote.model.SteamSchemaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApi {
    /**
     * Obtiene el esquema de un juego, incluyendo su lista de logros.
     * NOTA: Este endpoint oculta las descripciones de los logros marcados como "hidden": 1.
     */
    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getGameAchievementsLegacy(
        @Query("key") apiKey: String,
        @Query("appid") appId: String
    ): SteamSchemaResponse

    /**
     * Obtiene los logros de un juego usando el servicio de IPlayerService.
     * A diferencia del anterior, este endpoint suele devolver las descripciones
     * incluso de los logros ocultos.
     */
    @GET("IPlayerService/GetGameAchievements/v1/")
    suspend fun getGameAchievements(
        @Query("key") apiKey: String,
        @Query("appid") appId: String,
        @Query("language") language: String = "spanish"
    ): SteamAchievementResponse
}
