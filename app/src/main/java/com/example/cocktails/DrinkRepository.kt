package com.example.cocktails

import android.util.Log

class DrinkRepository(
    private val drinkInterface: DrinkInterface,
    private val api: ApiController
) {

    suspend fun fetchMealFromApi(id: String): Drink? {
        return try {
            val response = api.getMealById(id)
            println(response?.toString() ?: "No meal found or an error occurred")

            val cocktail = response.drinks.firstOrNull()?.convert()
            println("Fetched cocktail: $cocktail")
            cocktail


        } catch (e: Exception) {
            Log.e("MealRepository", "API error: ${e.message}")
            null
        }
    }

    suspend fun saveMealOffline(drink: Drink, favorite: Boolean = false, userId: String) {
        val entity = DrinkEntity(
            id = drink.id,
            name = drink.name,
            thumbnail = drink.thumb,
            instructions = drink.strInstructions,
            isFavorite = favorite,
            isOffline = true,
            userId = userId
        )
        drinkInterface.insertMeal(entity)
    }

    suspend fun setFavorite(id: String, isFavorite: Boolean, userId: String) {
        drinkInterface.setFavorite(id, isFavorite, userId)
        if (isFavorite) {
            val meal = drinkInterface.getMealById(id, userId)
            if (meal != null && !meal.isOffline) {
                drinkInterface.setOffline(id, true, userId)
            }
        }
    }

    suspend fun setOffline(id: String, isOffline: Boolean, userId: String) {
        drinkInterface.setOffline(id, isOffline, userId)
    }

    suspend fun getFavoriteMeals(userId: String) = drinkInterface.getFavoriteMeals(userId)

    suspend fun getOfflineMeals(userId: String) = drinkInterface.getOfflineMeals(userId)

    suspend fun getMealById(id: String, userId: String) = drinkInterface.getMealById(id, userId)
}