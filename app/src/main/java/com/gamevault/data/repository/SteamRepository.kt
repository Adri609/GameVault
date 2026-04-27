package com.gamevault.data.repository

import com.gamevault.BuildConfig
import com.gamevault.di.RetrofitClient
import com.gamevault.domain.model.Achievement

class SteamRepository {

    /**
     * Obtiene la lista de logros globales de un juego de Steam a partir de su AppID.
     */

    suspend fun getGameAchievements(appId: String): List<Achievement> {
        return try {
            // Llamar a la API de Steam
            val response = RetrofitClient.steamApi.getGameAchievements(
                apiKey = BuildConfig.STEAM_API_KEY,
                appId = appId
            )

            // Navegar por las capas del JSON y transformar el DTO en el modelo limpio
            response.game?.availableGameStats?.achievements?.map { dto ->
                Achievement(
                    name = dto.name,
                    title = dto.displayName ?: "Logro Desconocido",
                    description = dto.description ?: "Descripción no disponible.",
                    iconUrl = dto.icon ?: "",
                    isHidden = dto.hidden == 1 // En Steam, 1 significa que está oculto
                )
            } ?: emptyList() // Si la lista viene nula, devolver una lista vacía

        } catch (e: Exception) {
            android.util.Log.e("GameVault_Debug", "Error catastrófico en Steam: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
}