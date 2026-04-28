package com.gamevault.domain.usecase

import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase encargado de sincronizar la bóveda local con la nube.
 * - Sube juegos pendientes de sincronizar.
 * - Descarga juegos de la nube que no estén en local.
 */
@Singleton
class SyncVaultUseCase @Inject constructor(
    private val gameDao: GameDao,
    private val firestoreRepository: FirestoreRepository,
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
                    rating = entity.rating
                )
                firestoreRepository.saveGame(dto).onSuccess {
                    gameDao.insertGame(entity.copy(isSynced = true))
                }
            }

            // 2. Descargar juegos de la nube
            firestoreRepository.getUserVaultFromCloud().onSuccess { cloudGames ->
                cloudGames.forEach { dto ->
                    val entity = GameEntity(
                        id = dto.id,
                        userId = userId,
                        name = dto.name,
                        coverUrl = dto.coverUrl,
                        releaseDate = dto.releaseDate,
                        steamId = dto.steamId,
                        rating = dto.rating,
                        genres = emptyList(), // No vienen de Firebase para ahorrar espacio
                        platforms = emptyList(),
                        summary = null,
                        isSynced = true
                    )
                    gameDao.insertGame(entity)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
