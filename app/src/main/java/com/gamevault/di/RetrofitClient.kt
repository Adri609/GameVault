package com.gamevault.di

import com.gamevault.data.remote.IgdbApi
import com.gamevault.data.remote.TwitchAuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que gestiona las instancias de Retrofit para toda la aplicación.
 */

object RetrofitClient {

    private const val TWITCH_AUTH_BASE_URL = "https://id.twitch.tv/"
    private const val IGDB_BASE_URL = "https://api.igdb.com/"

    // Motor para la autenticación de Twitch
    val twitchAuthApi: TwitchAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(TWITCH_AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usar Gson para traducir el JSON
            .build()
            .create(TwitchAuthApi::class.java)
    }

    // Motor para consultar los juegos de IGDB
    val igdbApi : IgdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(IGDB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IgdbApi::class.java)
    }
}