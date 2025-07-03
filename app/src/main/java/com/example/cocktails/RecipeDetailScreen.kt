package com.example.cocktails

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Star


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    mealId: String,
    viewModel: DrinkViewModel,
    navController: NavController
) {
    var drink by remember { mutableStateOf<Drink?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    LaunchedEffect(mealId) {
        val local = userId?.let { viewModel.repository.getMealById(mealId, it) }
        drink = local?.let {
            isFavorite = it.isFavorite
            isOffline = it.isOffline
            it.instructions?.let { it1 ->
                Drink(
                    id = it.id,
                    name = it.name,
                    thumb = it.thumbnail,
                    strInstructions = it1,
                    ingredients = it.ingredients
                )
            }
        } ?: viewModel.repository.fetchMealFromApi(mealId)
    }

    drink?.let {
        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                TopBar(title = "Szczegóły", isMainScreen = false){
                    navController.navigate("main?message=") {
                        popUpTo("main?message=") { inclusive = true }
                    }
                }
            }

        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                Image(
                    painter = rememberAsyncImagePainter(it.thumb),
                    contentDescription = it.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)

                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        it.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = {
                            if (isLoggedIn) {
                                coroutineScope.launch {
                                    viewModel.toggleFavorite(it)
                                    val nowFavorite = !isFavorite
                                    isFavorite = nowFavorite
                                    if (nowFavorite && !isOffline) {
                                        isOffline = true
                                    }
                                }
                            } else {
                                Toast.makeText(context, "This action requires authentification", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Ulubione"
                            )
                        }

                        IconButton(onClick = {
                            if (isLoggedIn) {
                                coroutineScope.launch {
                                    if (isOffline) {
                                        viewModel.removeOffline(it.id)
                                    } else {
                                        viewModel.saveOffline(it)
                                    }
                                    isOffline = !isOffline
                                }
                            } else {
                                Toast.makeText(context, "This action requires authentification", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Zapisane"
                            )
                        }
                    }
                }

                // Ingredients Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF493B2) // Ingredients card color
                    ),
                    border = BorderStroke(width = 1.dp, color = Color.Black) // Black border
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Ingredients:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        it.ingredients.forEach { (ingredient, measure) ->
                            Text(
                                text = "• $ingredient - $measure",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Instructions Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB2F493) // Instructions card color
                    ),
                    border = BorderStroke(width = 1.dp, color = Color.Black) // Black border
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Instructions:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            it.strInstructions ?: "No instructions available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Send SMS with ingredients")
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Send SMS") },
                        text = {
                            Column {
                                Text("Enter your phone number: ")
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    placeholder = { Text("123456789") }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val smsBody = it.ingredients.joinToString("\n") { (ingredient, measure) ->
                                    "• $ingredient - $measure"
                                }
                                sendSms(context, phoneNumber, smsBody)
                                showDialog = false
                            }) {
                                Text("Send")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Decline")
                            }
                        }
                    )
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

fun sendSms(context: Context, phoneNumber: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        putExtra("sms_body", body)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        Toast.makeText(context, "Opening SMS", Toast.LENGTH_SHORT).show()
        context.startActivity(intent)
    }
}