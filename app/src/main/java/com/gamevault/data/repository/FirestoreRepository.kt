package com.gamevault.data.repository

import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    // Obtener la referencia a la colección del usuario actual
    private fun getUserVaultCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users")
            .document(uid)
            .collection("vault")
    }

    /**
     * Sube una imagen a Firebase Storage y devuelve su URL de descarga.
     */
    suspend fun uploadProfilePicture(uri: Uri): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Borrar imagen anterior si existe
            try {
                storage.reference.child("profile_pictures/$uid.jpg").delete().await()
            } catch (e: Exception) {
                // Si no existe imagen previa, ignorar el error
            }

            // Nombre único con timestamp para forzar URL nueva y evitar caché de Coil
            val filename = "profile_pictures/${uid}_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
     * Obtiene los datos del perfil del usuario actual.
     */
    suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Perfil no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza los datos del perfil del usuario.
     */
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            firestore.collection("users").document(uid).set(user, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina todos los datos del usuario de Firestore (documento de perfil y bóveda).
     */
    suspend fun deleteUserData(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val userRef = firestore.collection("users").document(uid)
            val vaultCollection = userRef.collection("vault")

            // 1. Borrar todos los juegos en la bóveda
            val vaultSnapshot = vaultCollection.get().await()
            val batch = firestore.batch()
            vaultSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            // 2. Borrar el documento de perfil del usuario
            userRef.delete().await()

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
