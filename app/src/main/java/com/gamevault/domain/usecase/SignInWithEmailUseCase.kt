package com.gamevault.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Caso de uso responsable de autenticar a un usuario existente mediante correo electrónico y contraseña.
 *
 * Aplica la regla de negocio crítica de la plataforma: bloquea el acceso a cualquier cuenta
 * que no haya validado previamente su dirección de correo electrónico, previniendo el acceso no autorizado
 * y el uso de cuentas fantasma/bots.
 *
 * @property auth Cliente de [FirebaseAuth] utilizado para la validación de credenciales.
 */
class SignInWithEmailUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    /**
     * Ejecuta el proceso de inicio de sesión seguro.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @param pass Contraseña del usuario.
     * @return [Result.success] si las credenciales son válidas y el correo está verificado.
     *         [Result.failure] si las credenciales fallan o el correo carece de verificación.
     */
    suspend operator fun invoke(email: String, pass: String): Result<Unit> {
        return try {
            // Firebase comprueba si el email y la contraseña coinciden
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user

            if (user != null) {
                // Refrescar el usuario para obtener su estado de verificación en tiempo real (evita falsos negativos por caché)
                user.reload().await()

                // Aplicar la regla de negocio de verificación
                if (user.isEmailVerified) {
                    Result.success(Unit)
                } else {
                    user.sendEmailVerification().await()
                    // Cerrar la sesión generada y bloqueamos el paso
                    auth.signOut()
                    Result.failure(Exception("Tu cuenta requiere verificación. Te acabamos de enviar un nuevo enlace a tu correo. ¡Revisa tu bandeja de entrada (y el Spam)!"))
                }
            } else {
                Result.failure(Exception("Error al resolver la identidad del usuario."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }
}