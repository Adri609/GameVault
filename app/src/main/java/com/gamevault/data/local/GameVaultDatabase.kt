package com.gamevault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gamevault.data.local.converters.StringListConverter
import com.gamevault.data.local.GameConverters
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity

/**
 * Base de datos principal de la aplicación construida sobre Room.
 *
 * Gestiona el almacenamiento persistente local de la colección de juegos del usuario,
 * actuando como la fuente de verdad principal (Single Source of Truth) para la interfaz.
 * Soporta el guardado de tipos complejos (Listas, Enums) mediante los conversores especificados.
 */
@Database(
    entities = [GameEntity::class],
    version = 5,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class, GameConverters::class)
abstract class GameVaultDatabase : RoomDatabase() {

    /**
     * Proporciona el Data Access Object (DAO) para interactuar con los juegos almacenados.
     *
     * @return La interfaz [GameDao] que expone las operaciones CRUD para la tabla de juegos.
     */
    abstract val gameDao: GameDao
}