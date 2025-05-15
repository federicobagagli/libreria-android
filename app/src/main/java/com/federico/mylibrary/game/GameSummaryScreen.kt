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
import java.util.Locale

@Composable
fun GameSummaryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var totalGames by remember { mutableStateOf(0) }
    var mostCommonGenre by remember { mutableStateOf<String?>(null) }
    var lastAddedGame by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("games")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            totalGames = snapshot.size()

            val genres = snapshot.documents.mapNotNull {
                it.getString("genre")?.trim()?.takeIf { g -> g.isNotEmpty() }?.lowercase()
            }
            mostCommonGenre = genres.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            lastAddedGame = snapshot.documents
                .mapNotNull { doc ->
                    val dateStr = doc.getString("addedDate") ?: return@mapNotNull null
                    val title = doc.getString("title") ?: return@mapNotNull null
                    runCatching {
                        sdf.parse(dateStr) to title
                    }.getOrNull()
                }
                .maxByOrNull { it.first }
                ?.second
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.total_games, totalGames))
                mostCommonGenre?.let {
                    Text(text = stringResource(R.string.most_common_genre, it))
                }
                lastAddedGame?.let {
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
