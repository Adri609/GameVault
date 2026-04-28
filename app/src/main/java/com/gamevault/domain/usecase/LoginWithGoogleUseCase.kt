package com.gamevault.domain.usecase

import com.gamevault.data.repository.AuthRepository
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Caso de uso para gestionar el inicio de sesión mediante Google Authentication.
 */
class LoginWithGoogleUseCase @Inject constructor (
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository
){
    /**
     * Autentica al usuario en Firebase usando un ID Token de Google y guarda sus datos.
     * 
     * @param idToken Token de identidad proporcionado por Google Sign-In.
     * @return [Result] con el objeto [User] en caso de éxito o una excepción en caso de error.
     */
    suspend operator fun invoke(idToken: String): Result<User> {
        return try {
            // Convertir el token de Google en una credencial de Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // Iniciar sesión en Firebase utilizando la credencial
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Crear el modelo de usuario con los datos de la cuenta de Google
                // Se intenta recuperar la foto de perfil que proporciona Google
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = firebaseUser.displayName ?: "Usuario de Google",
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                // Guardar o actualizar la información del usuario en Firestore
                authRepository.saveUserToFirestore(newUser)

                Result.success(newUser)
            } else {
                Result.failure(Exception("Error al iniciar sesión con Google."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
