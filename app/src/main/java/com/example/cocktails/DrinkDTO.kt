package com.example.cocktails

data class DrinkDTO(
    val idDrink: String,
    val strDrink: String,
    val strDrinkThumb: String,
    val strInstructions: String,
    val strDrinkAlternate: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?
) {
    fun convert(): Drink {
        val ingredients = mutableListOf<Pair<String, String>>()
        for (i in 1..10) { // Adjusted for the actual number of possible ingredients
            val ingredient = this::class.java.getDeclaredField("strIngredient$i").get(this) as? String
            val measure = this::class.java.getDeclaredField("strMeasure$i").get(this) as? String

            if (!ingredient.isNullOrEmpty()) {
                ingredients.add(Pair(ingredient.trim(), measure?.trim().orEmpty()))
            }
        }

        val drink = Drink(
                id = idDrink,
                name = strDrink,
                thumb = strDrinkThumb,
                strInstructions = strInstructions,
                ingredients = ingredients
        )

        return drink
    }
}