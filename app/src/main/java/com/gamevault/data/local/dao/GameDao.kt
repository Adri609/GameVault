package com.gamevault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gamevault.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos (DAO) para gestionar la Bóveda de juegos.
 */
@Dao
interface GameDao {
    /**
     * Obtiene todos los juegos de la bóveda, emitiendo actualizaciones en tiempo real.
     * Ordenado por 'dateAdded' para que los últimos que se añadan salgan primero.
     */
    @Query("SELECT * FROM favorite_games ORDER BY dateAdded DESC")
    fun getAllFavoriteGames(): Flow<List<GameEntity>>

    /**
     * Inserta un nuevo juego. Si ya existe, lo actualiza (útil para refrescar ratings).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    /**
     * Elimina un juego por su ID.
     */
    @Query("DELETE FROM favorite_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    /**
     * Comprueba si un juego ya está en la bóveda.
     * Esto nos servirá para cambiar el icono de "+" por un "Check" en la Home.
     */
    @Query("SELECT EXISTS(SELECT * FROM favorite_games WHERE id = :gameId)")
    suspend fun isGameInVault(gameId: Long): Boolean
}