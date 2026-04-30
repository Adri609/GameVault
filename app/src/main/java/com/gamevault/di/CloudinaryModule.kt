package com.gamevault.di

import android.content.Context
import com.cloudinary.android.MediaManager
import com.gamevault.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar la instancia de Cloudinary en toda la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    /**
     * Proporciona una instancia única de MediaManager configurada con las credenciales de Cloudinary.
     */
    @Provides
    @Singleton
    fun provideMediaManager(@ApplicationContext context: Context): MediaManager {
        android.util.Log.d("CloudinaryModule", "cloud_name: '${BuildConfig.CLOUDINARY_NAME}'")
        android.util.Log.d("CloudinaryModule", "api_key: '${BuildConfig.CLOUDINARY_API_KEY}'")

        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_CLIENT_SECRET
        )
        MediaManager.init(context, config)
        return MediaManager.get()
    }
}