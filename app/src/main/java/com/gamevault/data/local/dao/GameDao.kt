package com.gamevault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gamevault.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para gestionar operaciones CRUD de la tabla `favorite_games`.
 *
 * Define la interfaz para todas las consultas (queries) y operaciones que se pueden realizar
 * sobre la colección de juegos del usuario almacenada en la base de datos local (Room).
 *
 * ## Características
 * - **Reactividad**: Los métodos que devuelven `Flow` emiten actualizaciones automáticamente
 * - **Seguridad**: Las queries parametrizadas protegen contra inyección SQL
 * - **Performance**: Índices en `userId` y `id` para queries rápidas
 * - **Integridad**: Multi-usuario por clave compuesta `(id, userId)`
 *
 * ## Patrón de Uso
 * ```kotlin
 * @Inject
 * lateinit var gameDao: GameDao
 *
 * // Obtener todos los juegos y observar cambios
 * val games: Flow<List<GameEntity>> = gameDao.getAllFavoriteGames(userId)
 *     .collectLatest { updatedGames ->
 *         // Se ejecuta automáticamente cada vez que cambia la lista
 *     }
 * ```
 *
 * @see GameEntity
 * @see com.gamevault.data.local.GameVaultDatabase
 */
@Dao
interface GameDao {

    /**
     * Obtiene un juego específico con todos sus detalles.
     *
     * Emite automáticamente cada vez que el juego se actualiza (cambio de estado, rating, etc).
     * Útil para la pantalla de detalles del juego donde se necesita observar cambios en tiempo real.
     *
     * @param gameId ID del juego a obtener.
     * @param userId ID del usuario propietario.
     * @return Flow que emite el juego actual, o null si no existe.
     *
     * @sample
     * ```kotlin
     * val game: Flow<GameEntity?> = gameDao.getGameById(123L, "user@firebase.com")
     * ```
     */
    @Query("SELECT * FROM favorite_games WHERE id = :gameId AND userId = :userId LIMIT 1")
    fun getGameById(gameId: Long, userId: String): Flow<GameEntity?>

    /**
     * Obtiene todos los juegos de la bóveda de un usuario.
     *
     * Emite automáticamente cuando:
     * - Se agrega un nuevo juego
     * - Se elimina un juego
     * - Se modifica cualquier campo (estado, rating, favorito)
     *
     * Los juegos se ordenan por fecha de adición (más reciente primero).
     *
     * @param userId ID del usuario propietario.
     * @return Flow que emite la lista completa de juegos del usuario.
     *
     * @sample
     * ```kotlin
     * val allGames: Flow<List<GameEntity>> = gameDao.getAllFavoriteGames("user@firebase.com")
     * ```
     */
    @Query("SELECT * FROM favorite_games WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getAllFavoriteGames(userId: String): Flow<List<GameEntity>>

    /**
     * Inserta un nuevo juego o actualiza uno existente.
     *
     * Utiliza `OnConflictStrategy.REPLACE` para manejar conflictos de clave primaria.
     * Si el juego ya existe (mismo ID y userId), se replaza completamente.
     *
     * Esto es **suspendible** porque las escrituras son operaciones I/O.
     *
     * @param game Entidad GameEntity a insertar o actualizar.
     *
     * @sample
     * ```kotlin
     * val newGame = GameEntity(id = 1L, userId = "user@email.com", ...)
     * gameDao.insertGame(newGame)
     * ```
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    /**
     * Elimina un juego específico de la bóveda.
     *
     * Solo elimina si pertenece al usuario especificado (control multi-usuario).
     *
     * @param gameId ID del juego a eliminar.
     * @param userId ID del usuario propietario.
     *
     * @sample
     * ```kotlin
     * gameDao.deleteGameById(123L, "user@firebase.com")
     * ```
     */
    @Query("DELETE FROM favorite_games WHERE id = :gameId AND userId = :userId")
    suspend fun deleteGameById(gameId: Long, userId: String)

    /**
     * Verifica si un juego específico ya está en la bóveda.
     *
     * Emite automáticamente y devuelve `true` si existe, `false` en caso contrario.
     * Útil para UI que depende de si un juego está en la colección.
     *
     * @param gameId ID del juego a verificar.
     * @param userId ID del usuario propietario.
     * @return Flow que emite true/false según si el juego existe.
     *
     * @sample
     * ```kotlin
     * val isSaved: Flow<Boolean> = gameDao.isGameSaved(123L, "user@firebase.com")
     * ```
     */
    @Query("SELECT EXISTS(SELECT * FROM favorite_games WHERE id = :gameId AND userId = :userId)")
    fun isGameSaved(gameId: Long, userId: String): Flow<Boolean>

    /**
     * Obtiene todos los juegos que aún no han sido sincronizados con Firestore.
     *
     * Estos son juegos con cambios locales pendientes de subir a la nube.
     * Utilizado por [com.gamevault.domain.usecase.SyncVaultUseCase] para sincronizar automáticamente.
     *
     * @param userId ID del usuario propietario.
     * @return Lista suspendible de juegos sin sincronizar.
     *
     * @sample
     * ```kotlin
     * val unsyncedGames = gameDao.getUnsyncedGames("user@firebase.com")
     * ```
     */
    @Query("SELECT * FROM favorite_games WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedGames(userId: String): List<GameEntity>

    /**
     * Limpia completamente la bóveda de un usuario.
     *
     * **Operación irreversible** - Elimina TODOS los juegos del usuario.
     * Se utiliza principalmente cuando el usuario cierra sesión o solicita borrar datos.
     *
     * @param userId ID del usuario cuya bóveda se limpiar.
     *
     * @sample
     * ```kotlin
     * gameDao.clearVault("user@firebase.com")
     * ```
     */
    @Query("DELETE FROM favorite_games WHERE userId = :userId")
    suspend fun clearVault(userId: String)
}
