package com.gamevault.data.repository

import com.gamevault.data.remote.model.FirebaseGameDto
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val mediaManager: MediaManager
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

            android.util.Log.d("Cloudinary", "Iniciando subida para uid: $uid, uri: $uri")

            suspendCancellableCoroutine { continuation ->
                mediaManager.upload(uri)
                    .option("public_id", "profile_pictures/$uid")
                    .option("overwrite", true)
                    .option("resource_type", "image")
                    .callback(object : UploadCallback {
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            android.util.Log.d("Cloudinary", "Éxito: $resultData")
                            val url = resultData["secure_url"] as? String
                            if (url != null) {
                                continuation.resume(Result.success(url))
                            } else {
                                continuation.resume(Result.failure(Exception("URL no recibida")))
                            }
                        }
                        override fun onError(requestId: String?, error: ErrorInfo) {
                            android.util.Log.e("Cloudinary", "Error: ${error.description}, código: ${error.code}")
                            continuation.resume(Result.failure(Exception(error.description)))
                        }
                        override fun onStart(requestId: String) {
                            android.util.Log.d("Cloudinary", "Subida iniciada: $requestId")
                        }
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            android.util.Log.d("Cloudinary", "Progreso: $bytes / $totalBytes")
                        }
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("Cloudinary", "Reprogramado: ${error.description}")
                        }
                    })
                    .dispatch()
            }
        } catch (e: Exception) {
            android.util.Log.e("Cloudinary", "Excepción: ${e.message}", e)
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
