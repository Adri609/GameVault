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
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Error al iniciar sesión con Google."))

            // Comprobar si el usuario ya tiene perfil guardado en Firestore
            val existingUser = authRepository.getUserFromFirestore(firebaseUser.uid)

            if (existingUser != null) {
                // Usuario existente: NO tocar Firestore, devolver sus datos tal cual
                Result.success(existingUser)
            } else {
                // Usuario nuevo: crear perfil por primera vez
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = firebaseUser.displayName ?: "Usuario de Google",
                    profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                authRepository.saveUserToFirestore(newUser)
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
