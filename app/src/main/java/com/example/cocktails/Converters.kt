package com.example.cocktails

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIngredientsList(value: List<Pair<String, String>>): String {
        return value.joinToString("|") { "${it.first}::${it.second}" }
    }

    @TypeConverter
    fun toIngredientsList(value: String): List<Pair<String, String>> {
        if (value.isEmpty()) return emptyList()
        return value.split("|").map {
            val parts = it.split("::")
            if (parts.size == 2) Pair(parts[0], parts[1]) else Pair("", "")
        }
    }
}