package com.example.cocktails

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    drinkViewModel: DrinkViewModel,
    authViewModel: AuthViewModel = viewModel(),
    message: String? = null
) {
    val meals by drinkViewModel.drinks.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedScreen by remember { mutableStateOf("Wszystkie") }
    var favoriteMeals by remember { mutableStateOf(emptyList<DrinkEntity>()) }
    var offlineMeals by remember { mutableStateOf(emptyList<DrinkEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val context = LocalContext.current
    val isOnline = remember { isInternetAvailable(context) }

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var lastClickTime by remember { mutableStateOf(0L) }
    val drawerEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        drawerEnabled.value = true
    }

    LaunchedEffect(message) {
        message?.takeIf { it.isNotBlank() }?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        try {
            drinkViewModel.getFavoriteMeals { favoriteMeals = it }
            drinkViewModel.getOfflineMeals { offlineMeals = it }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Błąd ładowania danych")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(screenWidth * 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Divider()

                    DrawerItem("Wszystkie", Icons.Default.List) {
                        selectedScreen = "Wszystkie"
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Ulubione", Icons.Default.Favorite) {
                        navController.navigate("favorites")
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Zapisane", Icons.Default.Save) {
                        navController.navigate("offline")
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoggedIn) {
                        DrawerItem("Wyloguj się", Icons.Default.Logout) {
                            authViewModel.logout {
                                navController.navigate("login") {
                                    popUpTo("main?message={message}") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        DrawerItem("Zaloguj się", Icons.Default.Person) {
                            navController.navigate("login")
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopBar(
                    title = "Catalogue",
                    isMainScreen = true,
                    onNavigationClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime > 500 && drawerEnabled.value) {
                            lastClickTime = now
                            coroutineScope.launch {
                                if (!drawerState.isOpen) {
                                    drawerState.open()
                                }
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        if (it.isEmpty()) drinkViewModel.loadRandomMeals()
                        else drinkViewModel.search(it)
                    },
                    label = { Text("Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                when (selectedScreen) {
                    "Wszystkie" -> {
                        val safeMeals = meals ?: emptyList()
                        val displayMeals = if (isOnline) {
                            safeMeals.map {
                                DrinkEntity(
                                    id = it.id,
                                    name = it.name,
                                    thumbnail = it.thumb,
                                    instructions = it.strInstructions,
                                    isFavorite = favoriteMeals.any { fav -> fav.id == it.id },
                                    isOffline = offlineMeals.any { off -> off.id == it.id },
                                    userId = userId
                                )
                            }
                        } else offlineMeals

                        MealList(displayMeals, navController)
                    }
                    "Ulubione" -> MealList(favoriteMeals, navController)
                    "Zapisane" -> MealList(offlineMeals, navController)
                }
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun MealList(meals: List<DrinkEntity>, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Adjust the number of columns as needed
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp) // Add padding around the grid
    ) {
        items(meals) { meal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Ensures square cards
                    .clickable {
                        navController.navigate("details/${meal.id}")
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val context = LocalContext.current
					val resourceName = meal.name.lowercase().replace(" ", "_")
					val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                    AsyncImage(
                        model = resourceId,
                        contentDescription = meal.name,
                        placeholder = painterResource(R.drawable.cocktail_icon),
                        error = painterResource(R.drawable.cocktail_icon),
                        modifier = Modifier
                            .size(100.dp) // Thumbnail size
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1 // Truncate long names
                    )
                }
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnected
}
@Composable
fun FavoriteDrinksScreen(navController: NavController, drinkViewModel: DrinkViewModel) {
    val favoriteMealsState = remember { mutableStateOf(emptyList<DrinkEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var canNavigateBack by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            try {
                drinkViewModel.getFavoriteMeals { meals ->
                    favoriteMealsState.value = meals
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Błąd ładowania ulubionych")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(title = "Liked", isMainScreen = false, onNavigationClick = {
                if (canNavigateBack && navController.previousBackStackEntry != null) {
                    canNavigateBack = false
                    navController.popBackStack()
                    coroutineScope.launch {
                        delay(500)
                        canNavigateBack = true
                    }
                }
            })
        }
    )  { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding)) {
                MealList(favoriteMealsState.value, navController)
            }
        }
    }
}

@Composable
fun OfflineDrinksScreen(navController: NavController, drinkViewModel: DrinkViewModel) {
    val offlineMealsState = remember { mutableStateOf(emptyList<DrinkEntity>()) }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var canNavigateBack by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            try {
                drinkViewModel.getOfflineMeals { meals ->
                    offlineMealsState.value = meals
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Błąd ładowania zapisanych")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(title = "Zapisane", isMainScreen = false, onNavigationClick = {
                if (canNavigateBack && navController.previousBackStackEntry != null) {
                    canNavigateBack = false
                    navController.popBackStack()
                    coroutineScope.launch {
                        delay(500)
                        canNavigateBack = true
                    }
                }
            })
        }
    ) { padding ->
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding)) {
                MealList(offlineMealsState.value, navController)
            }
        }
    }
}