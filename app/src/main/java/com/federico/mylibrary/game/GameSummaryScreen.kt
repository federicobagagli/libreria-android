package com.federico.mylibrary.game

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GameSummaryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var totalGames by remember { mutableStateOf(0) }

    var totalBoardGames by remember { mutableStateOf(0) }
    var mostCommonBoardGenre by remember { mutableStateOf<String?>(null) }
    var lastBoardGame by remember { mutableStateOf<String?>(null) }

    var totalVideoGames by remember { mutableStateOf(0) }
    var mostCommonVideoGenre by remember { mutableStateOf<String?>(null) }
    var lastVideoGame by remember { mutableStateOf<String?>(null) }

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("games")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val all = snapshot.documents.mapNotNull { it.data }

            totalGames = all.size

            val boardGames = all.filter { it["type"] == "board" }
            val videoGames = all.filter { it["type"] == "videogame" }

            totalBoardGames = boardGames.size
            totalVideoGames = videoGames.size

            mostCommonBoardGenre = boardGames.mapNotNull {
                it["genre"]?.toString()?.trim()?.takeIf { g -> g.isNotEmpty() }
            }.groupingBy { it.lowercase() }.eachCount().maxByOrNull { it.value }?.key

            mostCommonVideoGenre = videoGames.mapNotNull {
                it["genre"]?.toString()?.trim()?.takeIf { g -> g.isNotEmpty() }
            }.groupingBy { it.lowercase() }.eachCount().maxByOrNull { it.value }?.key

            lastBoardGame = boardGames.mapNotNull {
                val date = it["addedDate"]?.toString()
                val title = it["title"]?.toString()
                runCatching { sdf.parse(date) to title }.getOrNull()
            }.maxByOrNull { it.first }?.second

            lastVideoGame = videoGames.mapNotNull {
                val date = it["addedDate"]?.toString()
                val title = it["title"]?.toString()
                runCatching { sdf.parse(date) to title }.getOrNull()
            }.maxByOrNull { it.first }?.second
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.summary), style = MaterialTheme.typography.headlineMedium)

        // Riepilogo totale
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.total_games, totalGames))
            }
        }

        // Card: Videogiochi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎮 " + stringResource(R.string.game_type_videogame), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.total_games, totalVideoGames))
                mostCommonVideoGenre?.let {
                    Text(text = stringResource(R.string.most_common_genre, it))
                }
                lastVideoGame?.let {
                    Text(text = stringResource(R.string.last_added_game, it))
                }
            }
        }

        // Card: Giochi da tavolo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎲 " + stringResource(R.string.game_type_board), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.total_games, totalBoardGames))
                mostCommonBoardGenre?.let {
                    Text(text = stringResource(R.string.most_common_genre, it))
                }
                lastBoardGame?.let {
                    Text(text = stringResource(R.string.last_added_game, it))
                }
            }
        }

        Button(onClick = { navController.navigate("game_advanced_summary") }) {
            Text(stringResource(R.string.advanced_summary_title))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}