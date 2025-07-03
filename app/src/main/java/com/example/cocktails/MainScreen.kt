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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape


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
                modifier = Modifier.width(screenWidth * 0.8f),
                drawerContainerColor = Color(0xFFCFD9F9)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(15.dp))
                    Divider()

                    DrawerItem("Catalogue", Icons.Default.MenuBook) {
                        selectedScreen = "Wszystkie"
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Favorite", Icons.Default.Star) {
                        navController.navigate("favorites")
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Saved", Icons.Default.Bookmark) {
                        navController.navigate("offline")
                        coroutineScope.launch { drawerState.close() }
                    }
                    DrawerItem("Settings", Icons.Default.Settings) {
                        navController.navigate("settings")
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoggedIn) {
                        DrawerItem("Log out", Icons.Default.Logout) {
                            authViewModel.logout {
                                navController.navigate("login") {
                                    popUpTo("main?message={message}") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        DrawerItem("Log in", Icons.Default.Person) {
                            navController.navigate("login")
                        }
                    }
                    DrawerItem("Help", Icons.Default.Help) {
                        navController.navigate("help")
                        coroutineScope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

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

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp), // Space between items
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp) // Padding around the list
                        ) {
                            // Search bar as the first item in the scrollable list
                            item {
                                OutlinedTextField(
                                    value = search,
                                    onValueChange = {
                                        search = it
                                        if (it.isEmpty()) drinkViewModel.loadPopularDrinks(listOf(
                                            "178325",
                                            "11113",
                                            "11117",
                                            "11288",
                                            "17211",
                                            "11006",
                                            "11007",
                                            "11728",
                                            "11000",
                                            "11003",
                                            "11001",
                                            "17207",
                                            "13621",
                                            "11004"
                                        ))
                                        else drinkViewModel.search(it)
                                    },
                                    label = { Text("Search") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp), // Add bottom padding for spacing with cards/group below
                                    shape = RoundedCornerShape(32.dp), // Rounded borders

                                )
                            }

                            // Cards displayed below the search bar
                            items(displayMeals) { meal ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("details/${meal.id}")
                                        },
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 8.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp), // Rounded corners
                                    border = BorderStroke(1.dp, Color.Black)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp), // Padding inside each item
                                        verticalAlignment = Alignment.CenterVertically
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
                                                .size(60.dp)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(10f)
                                                .padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = meal.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp), // Add spacing between items
        modifier = Modifier.padding(10.dp) // Add padding around the list
    ) {
        items(meals) { meal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("details/${meal.id}")
                    },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp), // Rounded corners
                border = BorderStroke(1.dp, Color.Black) // Black border with 2.dp stroke
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Padding inside each item
                    verticalAlignment = Alignment.CenterVertically
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
                            .size(60.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(10f)
                            .padding(start = 8.dp) // Padding between the image and the text
                    ) {
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
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