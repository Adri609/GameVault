package com.gamevault.data.mapper

import com.gamevault.data.local.entity.GameEntity
import com.gamevault.domain.model.Game
import com.gamevault.domain.model.GameStatus
import com.gamevault.domain.model.igdb.GameDto

/**
 * Objeto singleton que centraliza todos los mappers de conversión de datos.
 *
 * Responsable de transformar DTOs y entidades en modelos de dominio,
 * manteniendo una única fuente de verdad para las conversiones de datos.
 *
 * Siguiendo el patrón de arquitectura limpia, los mappers están separados
 * de la lógica de negocio y repositorios, facilitando el testing y mantenimiento.
 */
object GameMappers {

    /**
     * Convierte un [GameDto] de IGDB en un objeto de dominio [Game].
     *
     * Extrae la información relevante del DTO, construyendo la URL de la portada
     * y mapeando listas de géneros y plataformas de forma segura. Al ser datos
     * provenientes de una API externa, los campos de la bóveda del usuario
     * se inicializan con sus valores por defecto.
     *
     * @param gameDto Objeto DTO obtenido de la API de IGDB.
     * @return Objeto [Game] con la información transformada al modelo de dominio.
     *
     * @see GameDto
     * @see Game
     */
    fun gameFromDto(gameDto: GameDto): Game {
        return Game(
            id = gameDto.id,
            name = gameDto.name,
            coverUrl = gameDto.getCoverUrl(),
            rating = gameDto.rating,
            releaseDate = gameDto.firstReleasedDate,
            genres = gameDto.genres?.map { it.name } ?: emptyList(),
            platforms = gameDto.platforms?.map { it.name } ?: emptyList(),
            summary = gameDto.summary,
            status = GameStatus.NONE,
            personalRating = null,
            isFavorite = false
        )
    }

    /**
     * Convierte un [GameDto] de IGDB en un objeto [Game] con información detallada de Steam.
     *
     * Similar a [gameFromDto], pero incluye además la búsqueda de ID de Steam
     * a través de sitios web externos o ID de juegos externos.
     *
     * Este método es más pesado que [gameFromDto] y debe usarse solo cuando
     * se necesita la información de Steam (típicamente en detalles de juego).
     *
     * @param gameDto Objeto DTO obtenido de la API de IGDB.
     * @return Objeto [Game] con información completa incluyendo Steam ID si está disponible.
     *
     * @see gameFromDto
     */
    fun gameFromDtoWithSteamId(gameDto: GameDto): Game {
        val steamUrl = gameDto.websites?.find {
            it.url?.contains("steampowered.com", ignoreCase = true) == true
        }?.url

        var steamAppId: String? = null

        if (steamUrl != null) {
            val regex = """app/(\d+)""".toRegex()
            val match = regex.find(steamUrl)
            steamAppId = match?.groupValues?.get(1)
        }

        if (steamAppId == null) {
            steamAppId = gameDto.externalGames?.find { it.category == 1 }?.uid
        }

        return Game(
            id = gameDto.id,
            name = gameDto.name,
            coverUrl = gameDto.getCoverUrl(),
            rating = gameDto.rating,
            releaseDate = gameDto.firstReleasedDate,
            genres = gameDto.genres?.map { it.name } ?: emptyList(),
            platforms = gameDto.platforms?.map { it.name } ?: emptyList(),
            summary = gameDto.summary,
            steamId = steamAppId,
            status = GameStatus.NONE,
            personalRating = null,
            isFavorite = false
        )
    }

    /**
     * Convierte una lista de [GameDto] en una lista de objetos [Game].
     *
     * Operación en batch que aplica [gameFromDto] a cada elemento de la lista.
     * Útil para procesar respuestas de la API que contienen múltiples juegos.
     *
     * @param gameDtos Lista de DTOs de IGDB.
     * @return Lista de objetos [Game] mapeados.
     *
     * @see gameFromDto
     */
    fun gamesFromDtos(gameDtos: List<GameDto>): List<Game> {
        return gameDtos.map { gameFromDto(it) }
    }

    /**
     * Convierte una lista de [GameDto] en una lista de objetos [Game] con información de Steam.
     *
     * Similar a [gamesFromDtos], pero usa [gameFromDtoWithSteamId] para incluir
     * información de Steam en cada juego.
     *
     * Más costoso en rendimiento que [gamesFromDtos], usar solo cuando sea necesario.
     *
     * @param gameDtos Lista de DTOs de IGDB.
     * @return Lista de objetos [Game] con información completa.
     *
     * @see gameFromDtoWithSteamId
     * @see gamesFromDtos
     */
    fun gamesFromDtosWithSteamId(gameDtos: List<GameDto>): List<Game> {
        return gameDtos.map { gameFromDtoWithSteamId(it) }
    }

    /**
     * Convierte un [GameDto] en un objeto [Game] con solo metadatos (géneros y plataformas).
     *
     * Operación ligera usada para actualizar información de metadatos en juegos
     * que ya existen en la base de datos local, pero carecen de géneros/plataformas.
     *
     * Los campos como `coverUrl`, `rating`, `releaseDate` y `summary` se establecen como nulos.
     *
     * @param gameDto Objeto DTO con información de metadatos.
     * @return Objeto [Game] con solo metadatos rellenados.
     *
     * @see gameFromDto
     */
    fun gameMetadataFromDto(gameDto: GameDto): Game {
        return Game(
            id = gameDto.id,
            name = gameDto.name,
            coverUrl = null,
            rating = null,
            releaseDate = null,
            genres = gameDto.genres?.map { it.name } ?: emptyList(),
            platforms = gameDto.platforms?.map { it.name } ?: emptyList(),
            status = GameStatus.NONE,
            personalRating = null,
            isFavorite = false
        )
    }

    /**
     * Convierte una lista de [GameDto] en una lista de objetos [Game] con solo metadatos.
     *
     * Operación en batch que aplica [gameMetadataFromDto] a cada elemento.
     *
     * @param gameDtos Lista de DTOs con metadatos.
     * @return Lista de objetos [Game] con solo información de géneros y plataformas.
     *
     * @see gameMetadataFromDto
     */
    fun gamesMetadataFromDtos(gameDtos: List<GameDto>): List<Game> {
        return gameDtos.map { gameMetadataFromDto(it) }
    }
}

/**
 * Convierte una entidad de base de datos en un modelo de dominio.
 *
 * @receiver La entidad [GameEntity] obtenida de la base de datos local.
 * @return El modelo [Game] listo para ser consumido por la capa de UI.
 */
fun GameEntity.toDomainModel(): Game {
    return Game(
        id = this.id,
        name = this.name,
        coverUrl = this.coverUrl,
        rating = this.rating,
        releaseDate = this.releaseDate,
        genres = this.genres,
        platforms = this.platforms,
        summary = this.summary,
        steamId = this.steamId,
        status = this.status,
        personalRating = this.personalRating,
        isFavorite = this.isFavorite
    )
}

/**
 * Convierte un modelo de dominio en una entidad de base de datos.
 *
 * @receiver El modelo [Game] gestionado por la aplicación.
 * @param userId El identificador único del usuario al que pertenece el juego.
 * @param isSynced Indica si el juego ha sido sincronizado con la nube.
 * @return La entidad [GameEntity] lista para ser insertada o actualizada en Room.
 */
fun Game.toEntity(userId: String, isSynced: Boolean = false): GameEntity {
    return GameEntity(
        id = this.id,
        userId = userId,
        name = this.name,
        coverUrl = this.coverUrl,
        rating = this.rating,
        releaseDate = this.releaseDate,
        genres = this.genres,
        platforms = this.platforms,
        summary = this.summary,
        steamId = this.steamId,
        isSynced = isSynced,
        status = this.status,
        personalRating = this.personalRating,
        isFavorite = this.isFavorite
    )
}