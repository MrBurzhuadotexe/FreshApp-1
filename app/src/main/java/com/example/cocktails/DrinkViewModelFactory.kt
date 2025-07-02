package com.example.cocktails

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrinkViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrinkViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}