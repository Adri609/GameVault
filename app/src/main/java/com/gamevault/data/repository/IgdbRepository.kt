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

    // Guardar el token en memoria para evitar pedir uno nuevo en cada pantalla
    private var currentToken: String? = null

    /**
     * Obtiene un token válido de Twitch para autenticar las peticiones a IGDB.
     * Si ya existe un token en memoria, lo reutiliza.
     */
    private suspend fun getValidToken(): String {
        // Retornar el token si ya está en memoria
        currentToken?.let { return it }

        // Si no hay token, solicitar uno nuevo a Twitch
        val response = RetrofitClient.twitchAuthApi.getAccessToken(
            clientId = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        )

        currentToken = response.accessToken
        return response.accessToken
    }

    /**
     * Obtiene los juegos más populares del momento (últimos 6 meses con más votos).
     * @return Lista de objetos [Game].
     */
    suspend fun getPopularGames(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        // Obtener la fecha de hoy y la de hace 6 meses en segundos (Unix)
        val currentTimestamp = System.currentTimeMillis() / 1000
        val sixMonthsAgo = currentTimestamp - (180 * 24 * 60 * 60)

        // Consulta: Juegos recientes con más repercusión
        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date;
            where first_release_date > $sixMonthsAgo & first_release_date < $currentTimestamp & cover != null;
            sort rating_count desc;
            limit 12;
        """.trimIndent()

        val requestBody = queryText.toRequestBody("text/plain".toMediaTypeOrNull())

        // Realizar la petición y transformar los resultados a modelo de dominio
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
                releaseDate = apiGame.firstReleasedDate
            )
        }
    }

    /**
     * Obtiene los últimos lanzamientos ordenados por fecha descendente.
     * @return Lista de objetos [Game].
     */
    suspend fun getNewReleases(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        // Consulta para juegos recién salidos
        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date;
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
                releaseDate = apiGame.firstReleasedDate
            )
        }
    }

    /**
     * Obtiene los juegos más esperados (futuros lanzamientos con más hype).
     * @return Lista de objetos [Game].
     */
    suspend fun getAnticipatedGames(): List<Game> {
        val token = getValidToken()
        val authHeader = "Bearer $token"

        // Fecha actual para filtrar lanzamientos futuros
        val currentTimestamp = System.currentTimeMillis() / 1000

        // Consulta: Juegos futuros con portada y ordenados por expectación
        val queryText = """
            fields id, name, cover.image_id, rating, first_release_date;
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
                releaseDate = apiGame.firstReleasedDate
            )
        }
    }
}
