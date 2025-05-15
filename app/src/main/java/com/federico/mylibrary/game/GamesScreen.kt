package com.federico.mylibrary.game

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.export.GameExportItem
import com.federico.mylibrary.model.Game
import com.federico.mylibrary.viewmodel.GameFilterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

@Composable
fun GamesScreen(
    navController: NavController,
    exportViewModel: ExportViewModel,
    filterViewModel: GameFilterViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val filters by filterViewModel.filterState.collectAsState()
    val context = LocalContext.current

    var gamesRaw by remember { mutableStateOf<List<Pair<String, Game>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var gameToDelete by remember { mutableStateOf<Pair<String, Game>?>(null) }

    var sortField by remember { mutableStateOf("title") }
    var sortDirection by remember { mutableStateOf("asc") }
    var showFieldMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }

    val games by remember(sortField, sortDirection, gamesRaw) {
        derivedStateOf {
            val sorted = gamesRaw.sortedWith(compareBy { gameSortKey(it.second, sortField) })
            if (sortDirection == "desc") sorted.reversed() else sorted
        }
    }

    LaunchedEffect(userId, filters) {
        if (userId != null) {
            val snapshot = db.collection("games")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            gamesRaw = snapshot.documents.mapNotNull {
                val game = it.toObject<Game>()
                if (game != null) it.id to game else null
            }.filter { (_, game) ->
                fun match(input: String, filter: String) =
                    filter.isBlank() || input.contains(filter, ignoreCase = true)

                match(game.title, filters.title) &&
                        match(game.type, filters.type) &&
                        match(game.platform, filters.platform) &&
                        match(game.publisher, filters.publisher) &&
                        match(game.releaseDate, filters.releaseDate) &&
                        match(game.genre, filters.genre) &&
                        match(game.language, filters.language) &&
                        match(game.description, filters.description) &&
                        (filters.minPlayers.isBlank() || game.minPlayers.toString() == filters.minPlayers) &&
                        (filters.maxPlayers.isBlank() || game.maxPlayers.toString() == filters.maxPlayers) &&
                        (filters.durationMinutes.isBlank() || game.durationMinutes.toString() == filters.durationMinutes) &&
                        match(game.rating, filters.rating) &&
                        match(game.notes, filters.notes) &&
                        match(game.location, filters.location) &&
                        match(game.coverUrl, filters.coverUrl) &&
                        match(game.addedDate, filters.addedDate)
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else if (games.isEmpty()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.no_games_found))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        exportViewModel.setExportData(
                            items = games.map { (_, game) ->
                                GameExportItem(game)
                            },
                            fileName = "games_export.csv"
                        )
                        navController.navigate("exportView")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 " + stringResource(R.string.export_title_game))
                }

                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        Toast.makeText(context, context.getString(R.string.filters_cleared), Toast.LENGTH_SHORT).show()
                        navController.navigate("view_games") {
                            popUpTo("games") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄 " + stringResource(R.string.clear_filters))
                }

                Button(
                    onClick = { showFieldMenu = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.sort_button))
                }

                DropdownMenu(expanded = showFieldMenu, onDismissRequest = { showFieldMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_title)) }, onClick = {
                        sortField = "title"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_type)) }, onClick = {
                        sortField = "type"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_platform)) }, onClick = {
                        sortField = "platform"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_added_date)) }, onClick = {
                        sortField = "addedDate"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_rating)) }, onClick = {
                        sortField = "rating"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_genre)) }, onClick = {
                        sortField = "genre"; showFieldMenu = false; showDirectionMenu = true
                    })
                }

                DropdownMenu(expanded = showDirectionMenu, onDismissRequest = { showDirectionMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_asc)) }, onClick = {
                        sortDirection = "asc"; showDirectionMenu = false
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_desc)) }, onClick = {
                        sortDirection = "desc"; showDirectionMenu = false
                    })
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(games) { (id, game) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.game_title_label, game.title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.game_type_label, game.type))
                            Text(stringResource(R.string.game_platform_label, game.platform))
                            Text(stringResource(R.string.game_release_date_label, game.releaseDate))

                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("edit_game/$id")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                IconButton(onClick = {
                                    navController.navigate("details_game/$id")
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = "Details")
                                }

                                IconButton(onClick = {
                                    gameToDelete = id to game
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    gameToDelete?.let { (gameId, game) ->
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseFirestore.getInstance().collection("games").document(gameId)
                        .delete()
                        .addOnSuccessListener {
                            gamesRaw = gamesRaw.filterNot { it.first == gameId }
                        }
                    gameToDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text("\"${game.title}\"") }
        )
    }
}

fun gameSortKey(game: Game, field: String): Comparable<*> {
    return when (field) {
        "title" -> game.title.lowercase()
        "type" -> game.type.lowercase()
        "platform" -> game.platform.lowercase()
        "addedDate" -> game.addedDate
        "rating" -> game.rating.toIntOrNull() ?: 0
        "genre" -> game.genre.lowercase()
        else -> game.title.lowercase()
    }
}
