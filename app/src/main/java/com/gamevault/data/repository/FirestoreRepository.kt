package com.gamevault.data.repository

import com.gamevault.data.remote.model.FirebaseGameDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Obtener la referencia a la colección del usuario actual
    private fun getUserVaultCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vault")
    }

    /**
     * Guarda o actualiza un juego en la nube.
     */
    suspend fun saveGame(gameDto: FirebaseGameDto) : Result<Unit> {
        return try {
            val collection = getUserVaultCollection()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Usar el ID del juego como nombre del documento para evitar duplicados
            collection.document(gameDto.id.toString())
                .set(gameDto)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un juego de la nube.
     */
    suspend fun deleteGame(gameId: Long): Result<Unit> {
        return try {
            val collection = getUserVaultCollection()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            collection.document(gameId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga todos los juegos guardados en la nube para este usuario.
     */
    suspend fun getUserVaultFromCloud(): Result<List<FirebaseGameDto>> {
        return try {
            val collection = getUserVaultCollection()
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Obtener todos los documentos de la colección de golpe
            val snapshot = collection.get().await()

            // Transformar los documentos de Firebase a la clase FirebaseGameDto
            val gamesList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirebaseGameDto::class.java)
            }

            Result.success(gamesList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}