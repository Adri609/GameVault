package com.gamevault.di

import android.app.Application
import androidx.room.Room
import com.gamevault.data.local.GameVaultDatabase
import com.gamevault.data.local.dao.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la provisión de dependencias relacionadas con la base de datos local (Room).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provee la instancia única de la base de datos de la aplicación.
     */
    @Provides
    @Singleton
    fun provideGameVaultDatabase(app: Application): GameVaultDatabase {
        return Room.databaseBuilder(
            app,
            GameVaultDatabase::class.java,
            "gamevault_db"
        ).build()
    }

    /**
     * Provee el DAO necesario para realizar operaciones sobre la tabla de juegos.
     */
    @Provides
    @Singleton
    fun provideGameDao(db: GameVaultDatabase): GameDao {
        return db.gameDao
    }
}