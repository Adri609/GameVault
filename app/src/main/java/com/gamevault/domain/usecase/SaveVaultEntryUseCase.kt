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

        // Obtener el juego existente de la base de datos local para preservar campos como dateAdded
        val existingGame = try {
            gameDao.getGameById(game.id, userId).first()
        } catch (e: Exception) {
            null
        }

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

        val remoteResult = firestoreRepository.saveGame(firebaseGame)

        // Preservar el dateAdded original si el juego ya existe
        val dateAdded = existingGame?.dateAdded ?: System.currentTimeMillis()

        val entity = GameEntity(
            id = game.id,
            userId = userId,
            name = game.name,
            coverUrl = game.coverUrl,
            rating = game.rating,
            releaseDate = game.releaseDate,
            genres = game.genres,
            platforms = game.platforms,
            dateAdded = dateAdded,
            summary = game.summary,
            steamId = game.steamId,
            isSynced = remoteResult.isSuccess,
            status = game.status,
            personalRating = game.personalRating,
            isFavorite = game.isFavorite
        )

        // Log para verificar sincronización
        if (remoteResult.isSuccess) {
            android.util.Log.d("SaveVaultEntry", "Juego guardado exitosamente: ${game.name}, Favorito: ${game.isFavorite}")
        } else {
            android.util.Log.e("SaveVaultEntry", "Error guardando en Firestore: ${game.name}")
        }

        gameDao.insertGame(entity)
    }
}