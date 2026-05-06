package com.gamevault.domain.usecase

import com.gamevault.data.local.dao.GameDao
import com.gamevault.data.local.entity.GameEntity
import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.data.repository.FirestoreRepository
import com.gamevault.domain.model.Game
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase que centraliza la lógica de añadir o eliminar un juego de la bóveda,
 * sincronizando tanto la base de datos local (Room) como la nube (Firestore).
 */
class ToggleVaultUseCase @Inject constructor(
    private val gameDao: GameDao,
    private val firestoreRepository: FirestoreRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(game: Game) {
        val userId = auth.currentUser?.uid ?: return
        val isSaved = gameDao.isGameSaved(game.id, userId).first()

        if (isSaved) {
            // Eliminar de local
            gameDao.deleteGameById(game.id, userId)
            // Eliminar de remoto
            firestoreRepository.deleteGame(game.id)
        } else {
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
                favorite = game.isFavorite
            )

            // Intentar guardar en remoto primero o en paralelo
            val remoteResult = firestoreRepository.saveGame(firebaseGame)

            // El juego es nuevo, así que dateAdded será el tiempo actual
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

            if (remoteResult.isSuccess) {
                android.util.Log.d("ToggleVault", "Juego agregado exitosamente: ${game.name}")
            } else {
                android.util.Log.e("ToggleVault", "Error agregando juego a Firestore: ${game.name}")
            }

            gameDao.insertGame(entity)
        }
    }
}