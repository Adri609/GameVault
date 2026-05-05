package com.gamevault.data.local

import androidx.room.TypeConverter
import com.gamevault.domain.model.GameStatus

/**
 * Conversores de tipos personalizados para la base de datos Room de GameVault.
 *
 * Room solo admite tipos de datos primitivos por defecto (String, Int, Boolean, etc.).
 * Esta clase proporciona los métodos necesarios para transformar objetos complejos
 * o [Enum]s en tipos primitivos antes de guardarlos, y viceversa al leerlos.
 */
class GameConverters {

    /**
     * Convierte un [GameStatus] en un [String] para guardarlo en la base de datos.
     *
     * @param status El estado del juego a convertir.
     * @return El nombre exacto del enum (ej. "PLAYING", "COMPLETED").
     */
    @TypeConverter
    fun fromGameStatus(status: GameStatus): String {
        return status.name
    }

    /**
     * Convierte un [String] extraído de la base de datos de nuevo a su [GameStatus] original.
     *
     * @param name El nombre del estado guardado en SQLite.
     * @return El objeto [GameStatus] correspondiente, o [GameStatus.NONE] si hay un error o es nulo.
     */
    @TypeConverter
    fun toGameStatus(name: String): GameStatus {
        return try {
            GameStatus.valueOf(name)
        } catch (e: IllegalArgumentException) {
            GameStatus.NONE
        }
    }
}