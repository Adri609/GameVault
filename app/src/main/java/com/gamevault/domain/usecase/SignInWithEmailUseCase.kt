package com.gamevault.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val auth: FirebaseAuth
){
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