package com.gamevault.data.remote

import com.gamevault.domain.model.TwitchToken
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Interfaz de Retrofit para la autenticación con los servidores de Twitch (OAuth2).
 */
interface TwitchAuthApi {

    /**
     * Solicita un token de acceso a Twitch mediante el flujo "client_credentials".
     * @param clientId ID de cliente de la aplicación.
     * @param clientSecret Secreto de cliente de la aplicación.
     * @param grantType Tipo de concesión (por defecto "client_credentials").
     * @return Objeto [TwitchToken] con el token de acceso.
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): TwitchToken
}