package com.gamevault.data.repository

import com.gamevault.data.mapper.GameMappers
import com.gamevault.data.remote.IgdbApi
import com.gamevault.domain.model.Game
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de gestionar la comunicación con la API de IGDB.
 */
@Singleton
class IgdbRepository @Inject constructor(
    private val igdbApi: IgdbApi
) {

    /**
     * Obtiene una lista de juegos populares lanzados en los últimos 6 meses.
     */
    suspend fun getPopularGames(): List<Game> {
        val currentTimestamp = System.currentTimeMillis() / 1000
        val sixMonthsAgo = currentTimestamp - (180 * 24 * 60 * 60)

        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where first_release_date > $sixMonthsAgo & first_release_date < $currentTimestamp & cover != null;
            sort rating_count desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return GameMappers.gamesFromDtos(igdbApi.getGames(requestBody))
    }

    /**
     * Obtiene los lanzamientos más recientes con un mínimo de valoraciones.
     */
    suspend fun getNewReleases(): List<Game> {
        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where rating_count > 10 & cover != null;
            sort first_release_date desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return GameMappers.gamesFromDtos(igdbApi.getGames(requestBody))
    }

    /**
     * Obtiene juegos futuros con altas expectativas (hypes).
     */
    suspend fun getAnticipatedGames(): List<Game> {
        val currentTimestamp = System.currentTimeMillis() / 1000

        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where first_release_date > $currentTimestamp & cover != null & hypes != null;
            sort hypes desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return GameMappers.gamesFromDtos(igdbApi.getGames(requestBody))
    }

    /**
     * Busca juegos por nombre que no sean versiones secundarias y tengan portada.
     */
    suspend fun searchGames(query: String): List<Game> {
        return try {
            val queryText = """
                search "$query";
                fields id, name, cover.image_id, rating, first_release_date, genres.name, 
                platforms.name, summary;
                where version_parent = null & cover != null;
                limit 20;
            """.trimIndent()

            val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

            GameMappers.gamesFromDtos(igdbApi.getGames(requestBody))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene los detalles completos de un juego específico, incluyendo su sinopsis.
     */
    suspend fun getGameDetails(gameId: Long): Game? {
        return try {
            val queryText = """
                fields id, name, cover.image_id, rating, first_release_date, 
                genres.name, platforms.name, summary, 
                external_games.uid, external_games.category,
                websites.category, websites.url;
                where id = $gameId;
            """.trimIndent()

            val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = igdbApi.getGames(requestBody)

            response.firstOrNull()?.let { GameMappers.gameFromDtoWithSteamId(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene los metadatos (géneros y plataformas) de una lista de juegos por sus IDs.
     * Útil para reparar juegos sincronizados antiguos que no tenían esta info.
     */
    suspend fun getGamesMetadata(gameIds: List<Long>): List<Game> {
        if (gameIds.isEmpty()) return emptyList()
        
        return try {
            val idsString = gameIds.joinToString(",")
            val queryText = """
                fields id, genres.name, platforms.name;
                where id = ($idsString);
                limit 500;
            """.trimIndent()

            val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())
            
            GameMappers.gamesMetadataFromDtos(igdbApi.getGames(requestBody))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
