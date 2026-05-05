package com.gamevault.domain.model

/**
 * Representa el estado actual de un videojuego dentro de la Bóveda del usuario.
 *
 * Se utiliza para organizar la colección, aplicar filtros en la vista principal
 * y calcular las estadísticas avanzadas del [ProfileScreen].
 *
 * @property displayName Nombre legible y amigable para mostrar en la interfaz de usuario.
 */
enum class GameStatus(val displayName: String) {
    /** El juego está en la bóveda, pero no tiene un estado definido. */
    NONE("Ninguno"),

    /** El usuario está jugando activamente a este título. */
    PLAYING("Jugando"),

    /** El juego ha sido terminado por el usuario. */
    COMPLETED("Completado"),

    /** El juego está comprado/obtenido pero aún no se ha empezado. */
    PENDING("Pendiente"),

    /** El usuario empezó el juego pero decidió no terminarlo. */
    DROPPED("Abandonado"),

    /** El juego no está en posesión del usuario, pero lo quiere en un futuro. */
    WISHLIST("Deseado")
}