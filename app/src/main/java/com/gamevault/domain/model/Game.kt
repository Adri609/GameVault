package com.gamevault.domain.model

/**
 * Modelo central de dominio que representa un videojuego en la aplicación.
 *
 * Se utiliza en toda la capa de UI (Pantallas, Componentes, ViewModels) para
 * desacoplar la vista de los detalles de implementación de la API externa o la
 * base de datos local de Room.
 *
 * @param id Identificador único del videojuego.
 * @param name Nombre oficial del título.
 * @param coverUrl URL remota de la carátula del juego. Nulo si no existe o no se pudo cargar.
 * @param rating Puntuación o valoración media global obtenida de la comunidad/crítica (vía API).
 * @param releaseDate Fecha de lanzamiento original en formato timestamp (milisegundos).
 * @param genres Lista de etiquetas de género que categorizan el juego.
 * @param platforms Lista de nombres de plataformas en las que el juego fue publicado.
 * @param summary Texto descriptivo con el resumen de la historia o mecánicas del juego.
 * @param steamId ID interno de la plataforma Steam, usado para enlazar al perfil o tienda del juego.
 * @param status Etiqueta de clasificación del usuario en su bóveda (ej. Pendiente, Jugando). Define en qué "estante" está el juego.
 * @param personalRating Puntuación asignada exclusivamente por el usuario activo. Nulo si el usuario no lo ha calificado.
 * @param isFavorite `true` si el juego tiene la insignia de favorito del usuario, `false` por defecto.
 */
data class Game(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val rating: Double?,
    val releaseDate: Long?,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val summary: String? = null,
    val steamId: String? = null,
    val status: GameStatus = GameStatus.NONE,
    val personalRating: Float? = null,
    val isFavorite: Boolean = false
)