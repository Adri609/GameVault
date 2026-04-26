package com.gamevault.data.local.converters

import androidx.room.TypeConverter

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