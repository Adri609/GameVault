package com.gamevault.di

import com.gamevault.data.remote.IgdbApi
import com.gamevault.data.remote.IgdbInterceptor
import com.gamevault.data.remote.SteamApi
import com.gamevault.data.remote.TwitchAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TwitchRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IgdbRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SteamRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BasicClient

/**
 * Módulo de Hilt para gestionar las dependencias de red (Retrofit, OkHttp).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TWITCH_AUTH_BASE_URL = "https://id.twitch.tv/"
    private const val IGDB_BASE_URL = "https://api.igdb.com/"
    private const val STEAM_BASE_URL = "https://api.steampowered.com/"

    @Provides
    @Singleton
    @BasicClient
    fun provideBasicOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        @BasicClient basicClient: OkHttpClient,
        igdbInterceptor: IgdbInterceptor
    ): OkHttpClient {
        return basicClient.newBuilder()
            .addInterceptor(igdbInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @TwitchRetrofit
    fun provideTwitchRetrofit(@BasicClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TWITCH_AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @IgdbRetrofit
    fun provideIgdbRetrofit(@AuthenticatedClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IGDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @SteamRetrofit
    fun provideSteamRetrofit(@BasicClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(STEAM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTwitchAuthApi(@TwitchRetrofit retrofit: Retrofit): TwitchAuthApi {
        return retrofit.create(TwitchAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIgdbApi(@IgdbRetrofit retrofit: Retrofit): IgdbApi {
        return retrofit.create(IgdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSteamApi(@SteamRetrofit retrofit: Retrofit): SteamApi {
        return retrofit.create(SteamApi::class.java)
    }
}
