package com.gamevault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gamevault.data.local.converters.StringListConverter
import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract val gameDao: GameDao
}