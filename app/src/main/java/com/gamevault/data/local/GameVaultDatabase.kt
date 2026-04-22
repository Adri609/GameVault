package com.gamevault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity

/**
 * Base de datos principal de la aplicación utilizando Room.
 * Proporciona acceso al DAO para gestionar la persistencia de juegos.
 */
@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameVaultDatabase: RoomDatabase() {
    /**
     * Proveedor del DAO de juegos.
     */
    abstract val gameDao: GameDao
}