package com.gamevault.data.remote

import com.gamevault.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import javax.inject.Provider

/**
 * Clase encargada de gestionar el ciclo de vida del token de acceso de Twitch.
 */
@Singleton
class TokenManager @Inject constructor(
    private val twitchAuthApi: Provider<TwitchAuthApi>
) {
    private var currentToken: String? = null

    /**
     * Obtiene un token válido. Si no existe, lo solicita a la API.
     * Se usa runBlocking porque los interceptores de OkHttp se ejecutan en hilos de red
     * y no soportan directamente funciones suspendidas sin bloquear.
     */
    fun getAccessToken(): String? {
        currentToken?.let { return it }

        return synchronized(this) {
            currentToken?.let { return it }
            
            try {
                val response = runBlocking {
                    twitchAuthApi.get().getAccessToken(
                        clientId = BuildConfig.IGDB_CLIENT_ID,
                        clientSecret = BuildConfig.IGDB_CLIENT_SECRET
                    )
                }
                currentToken = response.accessToken
                currentToken
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
