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
     * Obtiene todos los juegos de la bóveda para un usuario específico.
     */
    @Query("SELECT * FROM favorite_games WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getAllFavoriteGames(userId: String): Flow<List<GameEntity>>

    /**
     * Inserta o actualiza un juego.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    /**
     * Elimina un juego de la bóveda de un usuario específico.
     */
    @Query("DELETE FROM favorite_games WHERE id = :gameId AND userId = :userId")
    suspend fun deleteGameById(gameId: Long, userId: String)

    /**
     * Comprueba si un juego ya está en la bóveda de un usuario específico.
     */
    @Query("SELECT EXISTS(SELECT * FROM favorite_games WHERE id = :gameId AND userId = :userId)")
    fun isGameSaved(gameId: Long, userId: String): Flow<Boolean>

    /**
     * Obtiene los juegos que no han sido sincronizados con la nube para un usuario.
     */
    @Query("SELECT * FROM favorite_games WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedGames(userId: String): List<GameEntity>
}
