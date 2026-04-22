package com.gamevault.data.repository

import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRepository @Inject constructor(
    // Inyectar las dependencias necesarias
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Referencia a la colección "users" que se creará en la base de datos
    private val usersCollection = firestore.collection("users")

    // Función para guardar un usuario en Firestore
    // suspend hace que la función sea llamada desde una corrutina
    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        // Guardar el documento usando el ID del usuario como nombre del documento
        return try {
            usersCollection.document(user.id).set(user).await() // Espera a que se complete la operación
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

        /*
        * .await() Es una función de las corrutinas de Kotlin que transforma
        * los callbacks de Firebase en código lineal y limpio,
        * evitando que la pantalla del móvil se congele mientras guarda los datos en internet.
        */
    }
}