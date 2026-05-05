package com.gamevault.data.local.converters

import androidx.room.TypeConverter

/**
 * Convertidor para permitir que Room almacene listas de strings como una cadena simple separada por comas.
 */
class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}