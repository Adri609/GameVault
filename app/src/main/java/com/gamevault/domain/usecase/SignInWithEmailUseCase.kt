package com.gamevault.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Caso de uso para gestionar el inicio de sesión tradicional mediante correo y contraseña.
 */
class SignInWithEmailUseCase @Inject constructor(
    private val auth: FirebaseAuth
){
    /**
     * Autentica al usuario en Firebase utilizando sus credenciales.
     * @param email Correo electrónico.
     * @param pass Contraseña.
     * @return [Result] indicando éxito o error de autenticación.
     */
    suspend operator fun invoke(email: String, pass: String): Result<Unit> {
        return try {
            // Firebase comprueba si el email y la contraseña coinciden
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }
}