package com.federico.mylibrary.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Game
import com.federico.mylibrary.ui.PieChartSection
import com.federico.mylibrary.ui.Legend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun GamePieChartsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var genreConsole by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var platformCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var publisherConsole by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var genreBoard by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var publisherBoard by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("games")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val allGames = snapshot.documents.mapNotNull { it.toObject(Game::class.java) }

            val videogames = allGames.filter { it.type.lowercase() == "videogame" }
            val boardgames = allGames.filter { it.type.lowercase() == "board" }

            genreConsole = videogames.mapNotNull { it.genre.lowercaseOrNull() }.groupingBy { it }.eachCount()
            platformCounts = videogames.mapNotNull { it.platform.lowercaseOrNull() }.groupingBy { it }.eachCount()
            publisherConsole = videogames.mapNotNull { it.publisher.lowercaseOrNull() }.groupingBy { it }.eachCount()

            genreBoard = boardgames.mapNotNull { it.genre.lowercaseOrNull() }.groupingBy { it }.eachCount()
            publisherBoard = boardgames.mapNotNull { it.publisher.lowercaseOrNull() }.groupingBy { it }.eachCount()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(R.string.pie_charts_title), style = MaterialTheme.typography.headlineMedium)

        // 🎮 Sezione VIDEOGIOCHI
        if (genreConsole.isNotEmpty() || platformCounts.isNotEmpty() || publisherConsole.isNotEmpty()) {
            Text(stringResource(R.string.videogame_section_title), style = MaterialTheme.typography.titleLarge)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (platformCounts.isNotEmpty()) {
                        Text(stringResource(R.string.platform_distribution), style = MaterialTheme.typography.titleMedium)
                        PieChartSection(platformCounts)
                        Legend(platformCounts, defaultLabel = stringResource(R.string.unknown_platform))
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    if (genreConsole.isNotEmpty()) {
                        Text(stringResource(R.string.genre_distribution), style = MaterialTheme.typography.titleMedium)
                        PieChartSection(genreConsole)
                        Legend(genreConsole)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    if (publisherConsole.isNotEmpty()) {
                        Text(stringResource(R.string.publisher_distribution), style = MaterialTheme.typography.titleMedium)
                        PieChartSection(publisherConsole)
                        Legend(publisherConsole)
                    }
                }
            }
        }

        // 🎲 Sezione GIOCHI DA TAVOLO
        if (genreBoard.isNotEmpty() || publisherBoard.isNotEmpty()) {
            Text(stringResource(R.string.boardgame_section_title), style = MaterialTheme.typography.titleLarge)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (genreBoard.isNotEmpty()) {
                        Text(stringResource(R.string.genre_distribution), style = MaterialTheme.typography.titleMedium)
                        PieChartSection(genreBoard)
                        Legend(genreBoard)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    if (publisherBoard.isNotEmpty()) {
                        Text(stringResource(R.string.publisher_distribution), style = MaterialTheme.typography.titleMedium)
                        PieChartSection(publisherBoard)
                        Legend(publisherBoard)
                    }
                }
            }
        }

        if (
            genreConsole.isEmpty() &&
            platformCounts.isEmpty() &&
            publisherConsole.isEmpty() &&
            genreBoard.isEmpty() &&
            publisherBoard.isEmpty()
        ) {
            Text(stringResource(R.string.no_data_available))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}

private fun String?.lowercaseOrNull(): String? =
    this?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
