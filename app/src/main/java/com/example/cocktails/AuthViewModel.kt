package com.example.cocktails

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val loginMessage = mutableStateOf<String?>(null)
    val showSnackbar = mutableStateOf(false)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            loginMessage.value = "Email and password cannot be empty"
            showSnackbar.value = true
            return
        }

        if (password.length < 6) {
            loginMessage.value = "Password must be at least 6 characters long"
            showSnackbar.value = true
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginMessage.value = "You are now logged in!"
                    showSnackbar.value = true
                    onSuccess()
                } else {
                    loginMessage.value = "Login error: ${task.exception?.localizedMessage ?: "Nieznany błąd"}"
                    showSnackbar.value = true
                }
            }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            loginMessage.value = "Email and password cannot be empty"
            showSnackbar.value = true
            return
        }

        if (password.length < 6) {
            loginMessage.value = "Password must be at least 6 characters long"
            showSnackbar.value = true
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginMessage.value = "Registration successful!"
                    showSnackbar.value = true
                    onSuccess()
                } else {
                    loginMessage.value = "Registrion error: ${task.exception?.localizedMessage ?: "Nieznany błąd"}"
                    showSnackbar.value = true
                }
            }
    }

    fun logout(onLoggedOut: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLoggedOut()
    }
}
