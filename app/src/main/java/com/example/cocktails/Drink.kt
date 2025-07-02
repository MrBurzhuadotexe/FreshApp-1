package com.example.cocktails

data class Drink(
    val id: String,
    val name: String,
    val thumb: String,
    val strInstructions: String,
    val ingredients: List<Pair<String, String>> = emptyList()
)