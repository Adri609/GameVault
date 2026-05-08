package com.gamevault.utils

/**
 * Envoltorio genérico que encapsula el resultado de operaciones asincrónicas.
 *
 * Permite representar de forma explícita los tres estados posibles de cualquier operación
 * (carga de red, consulta a BD, etc.): éxito con datos, error con mensaje, o proceso en curso.
 *
 * ## Uso Típico
 * ```kotlin
 * val state: StateFlow<Resource<List<Game>>> = repository.getGames()
 *     .map { games ->
 *         success { games }
 *     }
 *     .catch { exception ->
 *         error { exception.message ?: "Error desconocido" }
 *     }
 * ```
 *
 * @param T Tipo genérico del dato que se está cargando/obteniendo.
 * @property data Datos del resultado. Nulo en estados [Loading] o [Error] (opcional).
 * @property message Mensaje descriptivo. Usado principalmente en estados [Error].
 *
 * @see Success
 * @see Error
 * @see Loading
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    /**
     * Estado que representa una operación completada exitosamente.
     *
     * @param T Tipo del dato resultado.
     * @property data Datos obtenidos de la operación.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Estado que representa una operación que falló.
     *
     * @param T Tipo del dato incompleto (opcional).
     * @property message Descripción del error ocurrido.
     * @property data Datos parciales o anteriores (puede ser null).
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Estado que representa una operación en progreso.
     *
     * @param T Tipo del dato final esperado.
     * @property data Datos parciales disponibles mientras carga (opcional).
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}