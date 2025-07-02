package com.example.cocktails

import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    isMainScreen: Boolean,
    onNavigationClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.Black 
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = if (isMainScreen) Icons.Default.Menu else Icons.Default.ArrowBack,
                    contentDescription = if (isMainScreen) "Menu" else "Back",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.Black
        ),
        scrollBehavior = null
    )
}