package com.gamevault.data.remote

import com.gamevault.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor que añade automáticamente las cabeceras de autenticación necesarias para IGDB.
 */
@Singleton
class IgdbInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Solo añadir cabeceras si es una petición a IGDB
        if (!originalRequest.url.host.contains("igdb.com")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.getAccessToken()
        
        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Client-ID", BuildConfig.IGDB_CLIENT_ID)
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
