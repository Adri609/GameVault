package com.gamevault.data.remote.model

/**
 * Representación de un videojuego para almacenar en la base de datos NoSQL (Firebase Firestore).
 *
 * Contiene los datos esenciales para reconstruir la tarjeta del juego en la UI,
 * además de las estadísticas y preferencias personales del usuario.
 *
 * @param id Identificador único del juego (ID de IGDB).
 * @param name Nombre oficial del videojuego.
 * @param coverUrl URL de la carátula. Puede ser nulo si no hay imagen disponible.
 * @param releaseDate Fecha de lanzamiento en formato timestamp (milisegundos).
 * @param steamId Identificador del juego en la tienda de Steam, usado para obtener logros.
 * @param rating Nota media global proveniente de la API externa (ej. IGDB).
 * @param genres Lista de géneros principales asociados al juego.
 * @param platforms Lista de plataformas donde el juego está disponible.
 * @param addedAt Fecha en la que el usuario añadió el juego a su bóveda (timestamp).
 * @param status Estado de progreso del usuario con el juego (Guardado como String para evitar problemas de migración de Enums en Firebase).
 * @param personalRating Nota otorgada por el usuario al juego (de 1.0 a 5.0). Nulo si no lo ha valorado.
 * @param isFavorite Bandera que indica si el juego está marcado como favorito por el usuario.
 */
data class FirebaseGameDto(
    val id: Long = 0,
    val name: String = "",
    val coverUrl: String? = null,
    val releaseDate: Long? = null,
    val steamId: String? = null,
    val rating: Double? = null,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val addedAt: Long = System.currentTimeMillis(),
    val status: String = "NONE",
    val personalRating: Float? = null,
    val isFavorite: Boolean = false
)