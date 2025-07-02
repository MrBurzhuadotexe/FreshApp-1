package com.example.cocktails

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DrinkViewModel(application: Application) : AndroidViewModel(application) {

    private val _drinks = MutableStateFlow<List<Drink>>(emptyList())
    val drinks = _drinks.asStateFlow()

    private val database = AppDatabase.getDatabase(application)
    private val drinkDAO = database.mealDao()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.thecocktaildb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(ApiController::class.java)

    val repository = DrinkRepository(drinkDAO, api)

    val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    init {
        loadRandomMeals()
    }

    fun loadRandomMeals(count: Int = 10) {
        viewModelScope.launch {
            val randoms = mutableListOf<Drink>()
            repeat(count) {
                try {
                    val response = api.getRandomMeal()
                    println(response.toString())
                    response.drinks?.firstOrNull()?.let { // Check for null "drinks" and take the first drink if available
                        randoms.add(it.convert())
                    }
                } catch (e: Exception) {
                    println("API error: ${e.message}")
                }
            }
            _drinks.value = randoms // Update state with loaded drinks

        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val response = api.searchMeals(query)
                _drinks.value = response.drinks?.map { it.convert() } ?: emptyList()
            } catch (_: Exception) {
                _drinks.value = emptyList()
            }
        }
    }

    fun toggleFavorite(drink: Drink) {
        if (!isLoggedIn) {
            Log.d("MealViewModel", "Użytkownik niezalogowany – nie można dodać do ulubionych")
            return
        }

        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid == null) return@launch
            val existing = repository.getMealById(drink.id, uid)

            if (existing == null) {
                repository.saveMealOffline(drink, favorite = true, userId = uid)
            } else {
                val isNowFavorite = !existing.isFavorite
                repository.setFavorite(drink.id, isNowFavorite, uid)
            }
        }
    }

    fun saveOffline(drink: Drink) {
        if (!isLoggedIn) {
            Log.d("MealViewModel", "Użytkownik niezalogowany – nie można zapisać offline")
            return
        }

        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid != null) {
                val existing = repository.getMealById(drink.id, uid)
                if (existing == null) {
                    repository.saveMealOffline(drink, userId = uid)
                } else {
                    repository.setOffline(drink.id, true, uid)
                }
            }
        }
    }

    fun removeOffline(mealId: String) {
        viewModelScope.launch {
            val uid = getCurrentUserId()
            if (uid != null) {
                repository.setOffline(mealId, false, uid)
            }
        }
    }

    fun getFavoriteMeals(onResult: (List<DrinkEntity>) -> Unit) {
        viewModelScope.launch {
            getCurrentUserId()?.let { repository.getFavoriteMeals(it) }?.let { onResult(it) }
        }
    }

    fun getOfflineMeals(onResult: (List<DrinkEntity>) -> Unit) {
        viewModelScope.launch {
            getCurrentUserId()?.let { repository.getOfflineMeals(it) }?.let { onResult(it) }
        }
    }
}
