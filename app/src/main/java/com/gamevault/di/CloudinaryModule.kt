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
 * Módulo de Hilt que gestiona la configuración e inyección de dependencias de **Cloudinary**.
 *
 * Cloudinary es un servicio CDN especializado en almacenamiento y transformación de imágenes.
 * Se utiliza en GameVault para:
 * - Subir fotos de perfil de usuarios
 * - Almacenar imágenes personalizadas de juegos
 * - Aplicar transformaciones (resize, crop, filters) en el navegador
 *
 * ## Configuración Requerida
 * Las siguientes variables deben estar definidas en `local.properties`:
 * ```properties
 * CLOUDINARY_NAME=<cloud_name>
 * CLOUDINARY_API_KEY=<api_key>
 * CLOUDINARY_CLIENT_SECRET=<api_secret>
 * ```
 *
 * @see CloudinaryModule.provideMediaManager
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    /**
     * Proporciona una instancia única (singleton) de `MediaManager` de Cloudinary.
     *
     * Se encarga de:
     * 1. Leer las credenciales desde `BuildConfig` (inyectadas desde `local.properties`)
     * 2. Inicializar el SDK de Cloudinary con contexto de la aplicación
     * 3. Configurar el cliente para futuras operaciones de upload/transformación
     *
     * Los logs de depuración muestran los valores configurados (útil para validar setup).
     *
     * @param context Contexto de aplicación Android requerido por Cloudinary.
     * @return Instancia singleton de MediaManager lista para usar.
     *
     * @throws IllegalStateException Si las credenciales no están configuradas.
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