package com.example.cocktails

import androidx.room.*

@Dao
interface DrinkInterface {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add_drink(meal: DrinkEntity)

    @Query("SELECT * FROM meals")
    suspend fun fetch_all_drinks(): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE isFavorite = 1 AND userId = :userId")
    suspend fun fetch_liked_drinks(userId: String): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE isOffline = 1 AND userId = :userId")
    suspend fun fetch_saved_drinks(userId: String): List<DrinkEntity>

    @Query("SELECT * FROM meals WHERE id = :id AND userId = :userId")
    suspend fun fetch_by_id(id: String, userId: String): DrinkEntity?

    @Delete
    suspend fun remove_drink(meal: DrinkEntity)

    @Query("UPDATE meals SET isFavorite = :isFavorite WHERE id = :id AND userId = :userId")
    suspend fun setFavorite(id: String, isFavorite: Boolean, userId: String)

    @Query("UPDATE meals SET isOffline = :isOffline WHERE id = :id AND userId = :userId")
    suspend fun setOffline(id: String, isOffline: Boolean, userId: String)

    @Query("SELECT * FROM meals WHERE isFavorite = 1 AND userId = :userId")
    suspend fun get_liked_drinks_for_user(userId: String): List<DrinkEntity>
}