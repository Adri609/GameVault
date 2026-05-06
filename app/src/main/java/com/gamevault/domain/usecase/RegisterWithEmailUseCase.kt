package com.gamevault.domain.usecase

import com.gamevault.data.repository.AuthRepository
import com.gamevault.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Caso de uso responsable de registrar un nuevo usuario mediante correo electrónico y contraseña[cite: 4].
 *
 * Este caso de uso encapsula el flujo de seguridad completo para nuevas altas, garantizando que:
 * 1. Se cree la identidad en Firebase Authentication[cite: 4].
 * 2. Se dispare inmediatamente un correo de verificación para validar la autenticidad del email.
 * 3. Se persista el perfil público del usuario (modelo [User]) en Firestore[cite: 4].
 * 4. Se revoque la sesión activa de forma automática, obligando al usuario a confirmar su bandeja
 *    de entrada antes de poder acceder a la aplicación.
 *
 * @property auth Cliente de [FirebaseAuth] utilizado para la creación y gestión de la cuenta[cite: 4].
 * @property authRepository Repositorio encargado de la persistencia de los datos del usuario en la base de datos remota[cite: 4].
 */
class RegisterWithEmailUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository
) {
    /**
     * Ejecuta el proceso de registro seguro[cite: 4].
     *
     * @param email Dirección de correo electrónico proporcionada por el usuario[cite: 4].
     * @param pass Contraseña segura elegida por el usuario[cite: 4].
     * @param username Nombre de visualización público para el perfil del usuario[cite: 4].
     * @return Un objeto [Result] que contiene el modelo [User] si el registro y el envío del correo fueron exitosos,
     *         o encapsula la excepción en caso de que ocurra algún fallo durante el proceso[cite: 4].
     */
    suspend operator fun invoke(email: String, pass: String, username: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                firebaseUser.sendEmailVerification().await()

                val newUser = User(
                    id = firebaseUser.uid,
                    email = email,
                    username = username
                )

                authRepository.saveUserToFirestore(newUser)

                auth.signOut()

                Result.success(newUser)
            } else {
                Result.failure(Exception("Error crítico: No se pudo resolver la identidad del usuario tras el registro."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}