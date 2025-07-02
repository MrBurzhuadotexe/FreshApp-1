package com.example.cocktails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import com.example.cocktails.ui.theme.BlackBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Start the navigation to the main screen after the GIF duration
    LaunchedEffect(true) {
        delay(2500) // Adjust to match the duration of your GIF
        navController.navigate("main") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Display the GIF fullscreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center
    ) {
        // Use Coil's AsyncImage composable to display the GIF
        androidx.compose.foundation.Image(
            painter = rememberAsyncImagePainter(
                model = R.drawable.loading_animation,
                imageLoader = LocalContext.current.let { context ->
                    ImageLoader.Builder(context)
                        .components {
                            add(GifDecoder.Factory())
                        }
                        .build()
                }
            ),
            contentDescription = "Splash GIF",
            modifier = Modifier.size(130.dp)
        )
    }
}