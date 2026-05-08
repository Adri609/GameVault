package com.gamevault.data.local.converters

import androidx.room.TypeConverter

/**
 * Convertidor de tipos para Room que permite almacenar listas de strings.
 *
 * Dado que Room no soporta tipos complejos como `List<String>` nativamente,
 * este convertidor serializa la lista a una cadena de texto separada por comas
 * para almacenamiento en la BD, y deserializa en el sentido inverso.
 *
 * ## Formato Almacenado
 * ```
 * ["RPG", "Action", "Adventure"]  <->  "RPG,Action,Adventure"
 * ```
 *
 * ## Casos Especiales
 * - **Lista vacía**: Se detecta automáticamente y se devuelve `emptyList()`
 * - **Valores con comas**: No soportados (limitación del formato)
 */
class StringListConverter {
    /**
     * Deserializa una cadena separada por comas en una lista de strings.
     *
     * @param value Cadena en formato "item1,item2,item3"
     * @return Lista de strings, o lista vacía si el parámetro está vacío.
     */
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    /**
     * Serializa una lista de strings en una cadena separada por comas.
     *
     * @param list Lista de strings a serializar.
     * @return Cadena en formato "item1,item2,item3"
     */
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}