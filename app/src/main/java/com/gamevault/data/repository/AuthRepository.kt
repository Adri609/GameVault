package com.gamevault.data.repository

import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Repositorio encargado de gestionar la persistencia de datos de usuario en Firebase.
 */
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Referencia a la colección "users" en Firestore
    private val usersCollection = firestore.collection("users")

    /**
     * Guarda la información del usuario en Firestore.
     * @param user Objeto [User] con los datos a persistir.
     * @return [Result] indicando éxito o fallo.
     */
    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            // Guardar el documento usando el ID del usuario como identificador
            usersCollection.document(user.id).set(user).await() 
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}