package com.example.cocktails

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private val viewModel: DrinkViewModel by viewModels {
        DrinkViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        Log.d("Start", "poszlo!")

        setContent {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App(viewModel)
            }
        }
    }
}