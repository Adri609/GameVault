package com.gamevault.data.remote.model

/**
 * Representación ligera de un videojuego para almacenar en Firebase Firestore.
 * Incluye sólo lo necesario para reconstruir la tarjeta en la bóveda y
 * volver a consultar detalles si fuera necesario.
 */

data class FirebaseGameDto (
    val id: Long = 0,
    val name: String = "",
    val coverUrl: String? = null,
    val releaseDate: Long? = null,
    val steamId: String? = null,
    val rating: Double? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    // Constructor vacío para firebase, lo gener kotlin automáticamente al dar un valor por defecto a todo
}