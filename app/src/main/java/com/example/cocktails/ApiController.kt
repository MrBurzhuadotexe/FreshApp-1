package com.example.cocktails

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiController {
    @GET("random.php")
    suspend fun getRandomMeal(): ApiResponse

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): ApiResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): ApiResponse
}