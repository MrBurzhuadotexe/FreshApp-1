package com.example.cocktails

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class DrinkEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnail: String,
    val instructions: String?,
    val isFavorite: Boolean = false,
    val isOffline: Boolean = false,
    val userId: String = "",
    val ingredients: List<Pair<String, String>> = emptyList() // ← nowość
)