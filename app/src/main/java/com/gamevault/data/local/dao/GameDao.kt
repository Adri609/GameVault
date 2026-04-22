package com.gamevault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gamevault.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos (DAO) para gestionar los juegos favoritos.
 */
@Dao
interface GameDao {
    /**
     * Obtiene todos los juegos favoritos almacenados, emitiendo actualizaciones en tiempo real.
     */
    @Query("SELECT * FROM favorite_games")
    fun getAllFavoriteGames(): Flow<List<GameEntity>>

    /**
     * Inserta un nuevo juego en la base de datos. Si el juego ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    /**
     * Elimina un juego de la base de datos por su identificador.
     *
     * @param gameId Identificador del juego a eliminar.
     */
    @Query("DELETE FROM favorite_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)
}