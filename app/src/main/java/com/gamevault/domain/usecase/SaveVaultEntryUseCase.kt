package com.gamevault.domain.usecase

import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.Game
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Caso de uso responsable de insertar o actualizar un juego en la bóveda del usuario.
 *
 * Sincroniza los cambios tanto en la base de datos local (Room) como en la nube (Firestore),
 * gestionando el estado de sincronización (`isSynced`) en caso de fallos de red.
 */
class SaveVaultEntryUseCase @Inject constructor(
    private val gameDao: GameDao,
    private val firestoreRepository: FirestoreRepository,
    private val auth: FirebaseAuth
) {
    /**
     * Ejecuta el proceso de guardado o actualización.
     *
     * @param game El modelo de dominio [Game] con los datos actualizados que se desean persistir.
     */
    suspend operator fun invoke(game: Game) {
        val userId = auth.currentUser?.uid ?: return

        val firebaseGame = FirebaseGameDto(
            id = game.id,
            name = game.name,
            coverUrl = game.coverUrl,
            releaseDate = game.releaseDate,
            steamId = game.steamId,
            rating = game.rating,
            genres = game.genres,
            platforms = game.platforms,
            status = game.status.name,
            personalRating = game.personalRating,
            isFavorite = game.isFavorite
        )

        val remoteResult = firestoreRepository.saveGame(firebaseGame)

        val entity = GameEntity(
            id = game.id,
            userId = userId,
            name = game.name,
            coverUrl = game.coverUrl,
            rating = game.rating,
            releaseDate = game.releaseDate,
            genres = game.genres,
            platforms = game.platforms,
            summary = game.summary,
            steamId = game.steamId,
            isSynced = remoteResult.isSuccess,
            status = game.status,
            personalRating = game.personalRating,
            isFavorite = game.isFavorite
        )
        gameDao.insertGame(entity)
    }
}