package com.gamevault.data.remote

import com.gamevault.domain.model.igdb.GameDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz de Retrofit para interactuar con la API de IGDB.
 */
interface IgdbApi {
    /**
     * Consulta videojuegos en la API de IGDB mediante lenguaje de consulta Apocalix.
     * @param query Cuerpo de la petición con la consulta IGDB.
     * @return Lista de [GameDto].
     */
    @POST("v4/games")
    suspend fun getGames(
        @Body query: RequestBody
    ): List<GameDto>
}
