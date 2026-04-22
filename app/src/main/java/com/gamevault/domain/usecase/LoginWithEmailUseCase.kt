package com.gamevault.domain.usecase

import com.gamevault.data.repository.AuthRepository
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import jakarta.inject.Inject

/**
 * Caso de uso para gestionar el registro de usuarios mediante correo y contraseña.
 */
class LoginWithEmailUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository
) {
    /**
     * Registra un nuevo usuario en Firebase Auth y almacena su información en Firestore.
     *
     * @param email Correo electrónico.
     * @param pass Contraseña.
     * @param username Nombre de usuario.
     * @return [Result] con el [User] creado o error.
     */
    suspend operator fun invoke(email: String, pass: String, username: String): Result<User> {
        return try{
            // Se intenta registrar al usuario en Firebase auth
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // En caso de éxito, crear el usuario el modelo User
                val newUser = User(
                    id = firebaseUser.uid,
                    email = email,
                    username = username
                )

                // Guardar el usuario en la base de datos Firestone usando el repositorio
                authRepository.saveUserToFirestore(newUser)

                Result.success(newUser)
            } else {
                Result.failure(Exception("Error al crear el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}