package com.gamevault.data.local.entity

import androidx.room.Entity
import com.gamevault.domain.model.GameStatus

/**
 * Representa un videojuego almacenado en la base de datos local (La Bóveda del usuario).
 *
 * Esta entidad actúa como la fuente de verdad local para la colección del usuario,
 * permitiendo el funcionamiento sin conexión y un rendimiento ultrarrápido en la UI
 * antes de sincronizarse con Firebase.
 *
 * @param id Identificador único del videojuego (generalmente extraído de la API de IGDB).
 * @param userId Identificador único del usuario (UID de Firebase) que guardó el juego. Permite soporte multi-cuenta en el dispositivo.
 * @param name Nombre oficial del videojuego.
 * @param coverUrl URL directa a la imagen de la carátula o póster del juego. Puede ser nulo si la API no provee imagen.
 * @param rating Valoración media global obtenida de la base de datos externa (ej. IGDB). Es independiente de la nota del usuario.
 * @param releaseDate Fecha de lanzamiento original del juego en formato timestamp (milisegundos).
 * @param genres Lista de los géneros principales a los que pertenece el juego (RPG, Acción, etc.).
 * @param platforms Lista de las plataformas o consolas en las que el juego está disponible.
 * @param dateAdded Fecha y hora exacta (timestamp en milisegundos) en la que el usuario añadió el juego a su bóveda.
 * @param summary Sinopsis, resumen o descripción detallada de la trama y características del juego.
 * @param steamId Identificador único del juego en la tienda de Steam. Útil para integraciones futuras o enlaces a la tienda.
 * @param isSynced Bandera de control interno. `true` si este registro ya se ha subido correctamente a Firestore, `false` si hay cambios locales pendientes de subir.
 * @param status Estado de progreso actual asignado por el usuario (ej. Jugando, Completado, Abandonado).
 * @param personalRating Valoración o nota personal que el usuario le ha otorgado al juego tras jugarlo.
 * @param isFavorite Bandera que marca si el usuario ha destacado este juego como uno de sus favoritos de todos los tiempos.
 */
@Entity(
    tableName = "favorite_games",
    primaryKeys = ["id", "userId"]
)
data class GameEntity(
    val id: Long,
    val userId: String,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String>,
    val platforms: List<String>,
    val dateAdded: Long = System.currentTimeMillis(),
    val summary: String?,
    val steamId: String?,
    val isSynced: Boolean = false,
    val status: GameStatus = GameStatus.NONE,
    val personalRating: Float? = null,
    val isFavorite: Boolean = false
)