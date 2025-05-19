package com.federico.mylibrary.game

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Game
import com.federico.mylibrary.util.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun DetailsGameScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val gameId = backStackEntry.arguments?.getString("gameId") ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var game by remember { mutableStateOf<Game?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(gameId) {
        val doc = db.collection("games").document(gameId).get().await()
        game = doc.toObject(Game::class.java)
        Logger.d("COVER_URL", "URL = ${game?.coverUrl}")
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        game?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                @Composable
                fun info(label: Int, value: String) {
                    if (value.isNotBlank()) {
                        Text(text = "${stringResource(label)}: $value")
                    }
                }

                info(R.string.game_title, it.title)
                info(R.string.game_type, it.type)
                if (it.type.lowercase() == "videogame") {
                    info(R.string.game_platform, it.platform)
                }
                info(R.string.game_publisher, it.publisher)
                info(R.string.game_release_date, it.releaseDate)
                info(R.string.game_genre, it.genre)
                info(R.string.game_language, it.language)
                info(R.string.game_description, it.description)
                if (it.minPlayers > 0) info(R.string.game_min_players, it.minPlayers.toString())
                if (it.maxPlayers > 0) info(R.string.game_max_players, it.maxPlayers.toString())
                if (it.durationMinutes > 0) info(R.string.game_duration_minutes, it.durationMinutes.toString())
                info(R.string.game_rating, it.rating)
                info(R.string.game_notes, it.notes)
                info(R.string.game_location, it.location)
                info(R.string.game_added_date, it.addedDate)
                info(R.string.game_cover_url, it.coverUrl)

                if (it.coverUrl.isNotBlank()) {
                    val secureUrl = it.coverUrl.replace("http://", "https://")
                    AsyncImage(
                        model = secureUrl,
                        contentDescription = stringResource(R.string.game_cover_url),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(width = 100.dp, height = 150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.back))
                }
            }
        } ?: run {
            Text(
                text = stringResource(R.string.game_not_found),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
