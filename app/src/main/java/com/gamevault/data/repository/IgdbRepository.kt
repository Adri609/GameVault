package com.gamevault.data.repository

import com.gamevault.BuildConfig
import com.gamevault.di.RetrofitClient
import com.gamevault.domain.model.Game
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Repositorio encargado de gestionar la comunicación con la API de IGDB.
 */
class IgdbRepository {

    private var currentToken: String? = null

    /**
     * Obtiene un token de acceso válido de Twitch. Si ya existe uno en memoria, lo reutiliza.
     */
    private suspend fun getValidToken(): String {
        currentToken?.let { return it }

        val response = RetrofitClient.twitchAuthApi.getAccessToken(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        )

        currentToken = response.accessToken
        return response.accessToken
    }

    /**
     * Obtiene una lista de juegos populares lanzados en los últimos 6 meses.
     */
    suspend fun getPopularGames(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        val currentTimestamp = System.currentTimeMillis() / 1000
        val sixMonthsAgo = currentTimestamp - (180 * 24 * 60 * 60)

        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where first_release_date > $sixMonthsAgo & first_release_date < $currentTimestamp & cover != null;
            sort rating_count desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return RetrofitClient.igdbApi.getGames(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            authorization = authHeader,
            query = requestBody
        ).map { apiGame ->
            Game(
                id = apiGame.id,
                name = apiGame.name,
                coverUrl = apiGame.getCoverUrl(),
                rating = apiGame.rating,
                releaseDate = apiGame.firstReleasedDate,
                genres = apiGame.genres?.map { it.name } ?: emptyList(),
                platforms = apiGame.platforms?.map { it.name } ?: emptyList(),
                summary = apiGame.summary
            )
        }
    }

    /**
     * Obtiene los lanzamientos más recientes con un mínimo de valoraciones.
     */
    suspend fun getNewReleases(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where rating_count > 10 & cover != null;
            sort first_release_date desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return RetrofitClient.igdbApi.getGames(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            authorization = authHeader,
            query = requestBody
        ).map { apiGame ->
            Game(
                id = apiGame.id,
                name = apiGame.name,
                coverUrl = apiGame.getCoverUrl(),
                rating = apiGame.rating,
                releaseDate = apiGame.firstReleasedDate,
                genres = apiGame.genres?.map { it.name } ?: emptyList(),
                platforms = apiGame.platforms?.map { it.name } ?: emptyList(),
                summary = apiGame.summary
            )
        }
    }

    /**
     * Obtiene juegos futuros con altas expectativas (hypes).
     */
    suspend fun getAnticipatedGames(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        val currentTimestamp = System.currentTimeMillis() / 1000

        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
            where first_release_date > $currentTimestamp & cover != null & hypes != null;
            sort hypes desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        return RetrofitClient.igdbApi.getGames(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            authorization = authHeader,
            query = requestBody
        ).map { apiGame ->
            Game(
                id = apiGame.id,
                name = apiGame.name,
                coverUrl = apiGame.getCoverUrl(),
                rating = apiGame.rating,
                releaseDate = apiGame.firstReleasedDate,
                genres = apiGame.genres?.map { it.name } ?: emptyList(),
                platforms = apiGame.platforms?.map { it.name } ?: emptyList(),
                summary = apiGame.summary
            )
        }
    }

    /**
     * Busca juegos por nombre que no sean versiones secundarias y tengan portada.
     */
    suspend fun searchGames(query: String): List<Game> {
        return try {
            val token = getValidToken()
            val authHeader = "Bearer $token"

            val queryText = """
                search "$query";
                fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
                where version_parent = null & cover != null;
                limit 20;
            """.trimIndent()

            val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

            RetrofitClient.igdbApi.getGames(
                clientId = BuildConfig.IGDB_CLIENT_ID,
                authorization = authHeader,
                query = requestBody
            ).map { apiGame ->
                Game(
                    id = apiGame.id,
                    name = apiGame.name,
                    coverUrl = apiGame.getCoverUrl(),
                    rating = apiGame.rating,
                    releaseDate = apiGame.firstReleasedDate,
                    genres = apiGame.genres?.map { it.name } ?: emptyList(),
                    platforms = apiGame.platforms?.map { it.name } ?: emptyList(),
                    summary = apiGame.summary
                )
            }
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
            val token = getValidToken()
            val authHeader = "Bearer $token"

            val queryText = """
                fields id, name, cover.image_id, rating, first_release_date, genres.name, platforms.name, summary;
                where id = $gameId;
            """.trimIndent()

            val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitClient.igdbApi.getGames(
                clientId = BuildConfig.IGDB_CLIENT_ID,
                authorization = authHeader,
                query = requestBody
            )

            response.firstOrNull()?.let { apiGame ->
                Game(
                    id = apiGame.id,
                    name = apiGame.name,
                    coverUrl = apiGame.getCoverUrl(),
                    rating = apiGame.rating,
                    releaseDate = apiGame.firstReleasedDate,
                    genres = apiGame.genres?.map { it.name } ?: emptyList(),
                    platforms = apiGame.platforms?.map { it.name } ?: emptyList(),
                    summary = apiGame.summary
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}