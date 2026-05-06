package com.gamevault.domain.usecase

import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.data.repository.IgdbRepository
import com.gamevault.domain.model.GameStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase encargado de sincronizar la bóveda local con la nube (Firestore).
 *
 * Flujo de trabajo:
 * 1. Sube a la nube los juegos locales que están marcados como no sincronizados (`isSynced = false`),
 *    incluyendo los nuevos datos de interacción del usuario (estado, valoración y favoritos).
 * 2. Descarga todos los juegos guardados en la nube para mantener la base de datos local (Room) actualizada.
 * 3. Repara metadatos faltantes (como géneros o plataformas) consultando a IGDB para juegos antiguos
 *    que fueron guardados antes de implementar estas características.
 *
 * @property gameDao DAO para el acceso a la base de datos local.
 * @property firestoreRepository Repositorio para la interacción con Firebase.
 * @property igdbRepository Repositorio para la obtención de metadatos desde la API externa.
 * @property auth Autenticación de Firebase para identificar al usuario actual.
 */
@Singleton
class SyncVaultUseCase @Inject constructor(
    private val gameDao: GameDao,
    private val firestoreRepository: FirestoreRepository,
    private val igdbRepository: IgdbRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            // Subir juegos locales que no están sincronizados
            val unsyncedGames = gameDao.getUnsyncedGames(userId)
            unsyncedGames.forEach { entity ->
                val dto = FirebaseGameDto(
                    id = entity.id,
                    name = entity.name,
                    coverUrl = entity.coverUrl,
                    releaseDate = entity.releaseDate,
                    steamId = entity.steamId,
                    rating = entity.rating,
                    genres = entity.genres,
                    platforms = entity.platforms,
                    // Parámetros de personalización mapeados a Firestore
                    status = entity.status.name,
                    personalRating = entity.personalRating,
                    favorite = entity.isFavorite
                )
                firestoreRepository.saveGame(dto).onSuccess {
                    gameDao.insertGame(entity.copy(isSynced = true))
                    android.util.Log.d("SyncVault", "Juego sincronizado: ${entity.name}, Favorito: ${entity.isFavorite}")
                }.onFailure { error ->
                    android.util.Log.e("SyncVault", "Error sincronizando juego ${entity.name}: ${error.message}")
                }
            }

            // Descargar juegos de la nube
            val cloudResult = firestoreRepository.getUserVaultFromCloud()
            cloudResult.onSuccess { cloudGames ->
                // Identificar juegos que vienen sin metadatos (antiguos)
                val gamesToRepair = cloudGames.filter { it.genres.isEmpty() && it.platforms.isEmpty() }

                // Si hay juegos para reparar, pedimos la info a IGDB
                val repairedMetadata = if (gamesToRepair.isNotEmpty()) {
                    igdbRepository.getGamesMetadata(gamesToRepair.map { it.id })
                } else emptyList()

                cloudGames.forEach { dto ->
                    // Obtener el juego existente para preservar dateAdded
                    val existingEntity = try {
                        gameDao.getGameById(dto.id, userId).first()
                    } catch (e: Exception) {
                        null
                    }

                    val repairInfo = repairedMetadata.find { it.id == dto.id }

                    // Parseo seguro del estado por si la nube devuelve un String no válido o antiguo
                    val safeStatus = try {
                        GameStatus.valueOf(dto.status)
                    } catch (e: Exception) {
                        GameStatus.NONE
                    }

                    // Preservar dateAdded si el juego ya existe localmente
                    val dateAdded = existingEntity?.dateAdded ?: System.currentTimeMillis()

                    val entity = GameEntity(
                        id = dto.id,
                        userId = userId,
                        name = dto.name,
                        coverUrl = dto.coverUrl,
                        releaseDate = dto.releaseDate,
                        steamId = dto.steamId,
                        rating = dto.rating,
                        genres = dto.genres.ifEmpty { repairInfo?.genres ?: emptyList() },
                        platforms = dto.platforms.ifEmpty { repairInfo?.platforms ?: emptyList() },
                        dateAdded = dateAdded,
                        summary = null,
                        isSynced = true,
                        // Parámetros de personalización recuperados en Room
                        status = safeStatus,
                        personalRating = dto.personalRating,
                        isFavorite = dto.favorite
                    )
                    gameDao.insertGame(entity)

                    android.util.Log.d("SyncVault", "Juego descargado: ${dto.name}, Favorito: ${dto.favorite}, Estado: ${dto.status}")

                    if (repairInfo != null) {
                        val repairedDto = dto.copy(
                            genres = repairInfo.genres,
                            platforms = repairInfo.platforms
                        )
                        firestoreRepository.saveGame(repairedDto)
                    }
                }
            }.onFailure { error ->
                android.util.Log.e("SyncVault", "Error descargando bóveda: ${error.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("SyncVault", "Excepción en sincronización: ${e.message}", e)
            Result.failure(e)
        }
    }
}