package com.gamevault.di

import com.gamevault.BuildConfig
import com.gamevault.data.remote.IgdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Módulo de Hilt para gestionar las dependencias de red (Retrofit, OkHttp).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provee un cliente OkHttp configurado con interceptores para logging y autenticación.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Interceptor para depurar las peticiones en el logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provee la instancia de Retrofit configurada para la API de IGDB.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.igdb.com/v4/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provee la interfaz de la API de IGDB.
     */
    @Provides
    @Singleton
    fun provideIgdbApi(retrofit: Retrofit): IgdbApi {
        return retrofit.create(IgdbApi::class.java)
    }
}