package com.gamevault.data.remote

import com.gamevault.data.remote.model.SteamSchemaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApi {
    /**
     * Obtiene el esquema de un juego, incluyendo su lista de logros.
     */
    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getGameAchievements(
        @Query("key") apiKey: String,
        @Query("appid") appId: String
    ): SteamSchemaResponse
}