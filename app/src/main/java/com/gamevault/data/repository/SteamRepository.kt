package com.gamevault.data.repository

import com.gamevault.BuildConfig
import com.gamevault.data.remote.SteamApi
import com.gamevault.domain.model.Achievement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SteamRepository @Inject constructor(
    private val steamApi: SteamApi
) {

    /**
     * Obtiene la lista de logros de un juego de Steam a partir de su AppID.
     * Utiliza el endpoint de IPlayerService para asegurar que las descripciones
     * de los logros ocultos estén presentes.
     */
    suspend fun getGameAchievements(appId: String): List<Achievement> {
        return try {
            // Llamar a la API de Steam usando el endpoint de IPlayerService
            val response = steamApi.getGameAchievements(
                apiKey = BuildConfig.STEAM_API_KEY,
                appId = appId
            )

            // Transformar la respuesta en el modelo de dominio Achievement
            response.response?.achievements?.map { dto ->
                // Priorizamos display_description y evitamos strings vacíos
                val finalDescription = dto.displayDescription?.takeIf { it.isNotBlank() }
                    ?: dto.description?.takeIf { it.isNotBlank() }
                    ?: "Descripción no disponible."

                Achievement(
                    name = dto.name,
                    title = dto.displayName?.takeIf { it.isNotBlank() } ?: "Logro Desconocido",
                    description = finalDescription,
                    iconUrl = dto.icon ?: "",
                    isHidden = dto.hidden == 1
                )
            } ?: emptyList()

        } catch (e: Exception) {
            android.util.Log.e("GameVault_Debug", "Error al obtener logros de Steam: ${e.message}", e)
            
            // Fallback: Si el nuevo falla, intentamos el método legacy
            try {
                val legacyResponse = steamApi.getGameAchievementsLegacy(
                    apiKey = BuildConfig.STEAM_API_KEY,
                    appId = appId
                )
                legacyResponse.game?.availableGameStats?.achievements?.map { dto ->
                    Achievement(
                        name = dto.name,
                        title = dto.displayName ?: "Logro Desconocido",
                        description = dto.description ?: "Descripción no disponible.",
                        iconUrl = dto.icon ?: "",
                        isHidden = dto.hidden == 1
                    )
                } ?: emptyList()
            } catch (innerE: Exception) {
                emptyList()
            }
        }
    }
}
