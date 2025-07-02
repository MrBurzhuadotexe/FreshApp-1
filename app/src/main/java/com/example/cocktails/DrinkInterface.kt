package com.example.cocktails

import androidx.room.*

@Dao
interface DrinkInterface {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: DrinkEntity)

    @Query("SELECT * FROM meals")
    suspend fun getAllMeals(): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE isFavorite = 1 AND userId = :userId")
    suspend fun getFavoriteMeals(userId: String): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE isOffline = 1 AND userId = :userId")
    suspend fun getOfflineMeals(userId: String): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE id = :id AND userId = :userId")
    suspend fun getMealById(id: String, userId: String): DrinkEntity?

    @Delete
    suspend fun deleteMeal(meal: DrinkEntity)

    @Query("UPDATE meals SET isFavorite = :isFavorite WHERE id = :id AND userId = :userId")
    suspend fun setFavorite(id: String, isFavorite: Boolean, userId: String)

    @Query("UPDATE meals SET isOffline = :isOffline WHERE id = :id AND userId = :userId")
    suspend fun setOffline(id: String, isOffline: Boolean, userId: String)

    @Query("SELECT * FROM meals WHERE isFavorite = 1 AND userId = :userId")
    suspend fun getFavoriteMealsForUser(userId: String): List<DrinkEntity>
}