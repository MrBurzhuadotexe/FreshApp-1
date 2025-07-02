package com.example.cocktails
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.cocktails.ui.theme.AppTheme

@Composable
fun App(viewModel: DrinkViewModel) {
    AppTheme {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "splash") {

            composable("splash") {
                SplashScreen(navController)
            }

            composable(
                route = "main?message={message}",
                arguments = listOf(
                    navArgument("message") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val message = backStackEntry.arguments?.getString("message")
                MainScreen(navController, drinkViewModel = viewModel, message = message)
            }

            composable("details/{mealId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("mealId") ?: ""
                RecipeDetailScreen(mealId = id, viewModel = viewModel, navController = navController)
            }

            composable("login") {
                LoginScreen(navController)
            }

            composable("register") {
                RegisterScreen(navController)
            }

            composable("favorites") {
                FavoriteDrinksScreen(navController, drinkViewModel = viewModel)
            }

            composable("offline") {
                OfflineDrinksScreen(navController, drinkViewModel = viewModel)
            }
        }
    }
}