package com.gamevault.domain.usecase

import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.data.repository.IgdbRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase encargado de sincronizar la bóveda local con la nube.
 * - Sube juegos pendientes de sincronizar.
 * - Descarga juegos de la nube.
 * - Repara metadatos faltantes (géneros/plataformas) para juegos antiguos.
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
            // 1. Subir juegos locales que no están sincronizados
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
                    platforms = entity.platforms
                )
                firestoreRepository.saveGame(dto).onSuccess {
                    gameDao.insertGame(entity.copy(isSynced = true))
                }
            }

            // 2. Descargar juegos de la nube
            val cloudResult = firestoreRepository.getUserVaultFromCloud()
            cloudResult.onSuccess { cloudGames ->
                // Identificar juegos que vienen sin metadatos (antiguos)
                val gamesToRepair = cloudGames.filter { it.genres.isEmpty() && it.platforms.isEmpty() }
                
                // Si hay juegos para reparar, pedimos la info a IGDB
                val repairedMetadata = if (gamesToRepair.isNotEmpty()) {
                    igdbRepository.getGamesMetadata(gamesToRepair.map { it.id })
                } else emptyList()

                cloudGames.forEach { dto ->
                    val repairInfo = repairedMetadata.find { it.id == dto.id }
                    
                    val entity = GameEntity(
                        id = dto.id,
                        userId = userId,
                        name = dto.name,
                        coverUrl = dto.coverUrl,
                        releaseDate = dto.releaseDate,
                        steamId = dto.steamId,
                        rating = dto.rating,
                        genres = if (dto.genres.isNotEmpty()) dto.genres else repairInfo?.genres ?: emptyList(),
                        platforms = if (dto.platforms.isNotEmpty()) dto.platforms else repairInfo?.platforms ?: emptyList(),
                        summary = null,
                        isSynced = true
                    )
                    gameDao.insertGame(entity)
                    
                    // Si lo reparamos localmente, lo actualizamos también en Firebase para el futuro
                    if (repairInfo != null) {
                        val repairedDto = dto.copy(
                            genres = repairInfo.genres,
                            platforms = repairInfo.platforms
                        )
                        firestoreRepository.saveGame(repairedDto)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
