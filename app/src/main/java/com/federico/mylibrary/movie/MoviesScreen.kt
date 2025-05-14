// Contenuto completo aggiornato con blocco export corretto
// (esattamente come nel file fornito da te, ma con export funzionante)
package com.federico.mylibrary.movie

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
import com.federico.mylibrary.model.Movie
import com.federico.mylibrary.viewmodel.MovieFilterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.federico.mylibrary.export.MovieExportItem

@Composable
fun MoviesScreen(
    navController: NavController,
    exportViewModel: ExportViewModel,
    filterViewModel: MovieFilterViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val filters by filterViewModel.filterState.collectAsState()
    val context = LocalContext.current

    var moviesRaw by remember { mutableStateOf<List<Pair<String, Movie>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var movieToDelete by remember { mutableStateOf<Pair<String, Movie>?>(null) }
    var sortField by remember { mutableStateOf("title") }
    var sortDirection by remember { mutableStateOf("asc") }
    var showFieldMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }

    val movies by remember(sortField, sortDirection, moviesRaw) {
        derivedStateOf {
            val sorted = moviesRaw.sortedWith(compareBy { movieSortKey(it.second, sortField) })
            if (sortDirection == "desc") sorted.reversed() else sorted
        }
    }

    LaunchedEffect(userId, filters) {
        if (userId != null) {
            val snapshot = db.collection("movies")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            moviesRaw = snapshot.documents.mapNotNull {
                val movie = it.toObject<Movie>()
                if (movie != null) it.id to movie else null
            }.filter { (_, movie) ->
                fun match(input: String, filter: String) =
                    filter.isBlank() || input.contains(filter, ignoreCase = true)

                match(movie.title, filters.title) &&
                        match(movie.director, filters.director) &&
                        match(movie.genre, filters.genre) &&
                        match(movie.language, filters.language) &&
                        match(movie.publishDate, filters.publishDate) &&
                        match(movie.description, filters.description) &&
                        match(movie.productionCompany, filters.productionCompany) &&
                        (filters.duration.isBlank() || movie.duration.toString() == filters.duration) &&
                        match(movie.format, filters.format) &&
                        (filters.rating.isBlank() || movie.rating == filters.rating) &&
                        match(movie.notes, filters.notes) &&
                        match(movie.coverUrl, filters.coverUrl) &&
                        match(movie.location, filters.location)
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else if (movies.isEmpty()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.no_movies_found))
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
                        val exportItems = movies.map { (_, movie) ->
                            MovieExportItem(
                                title = movie.title,
                                originalTitle = movie.originalTitle,
                                director = movie.director,
                                cast = movie.cast,
                                productionCompany = movie.productionCompany,
                                genre = movie.genre,
                                language = movie.language,
                                description = movie.description,
                                publishDate = movie.publishDate,
                                duration = movie.duration,
                                format = movie.format,
                                rating = movie.rating,
                                notes = movie.notes,
                                coverUrl = movie.coverUrl,
                                location = movie.location
                            )
                        }
                        exportViewModel.setExportData(exportItems, "movies_export.csv")
                        navController.navigate("exportView")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 " + stringResource(R.string.export_title_movie))
                }

                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        Toast.makeText(context, context.getString(R.string.filters_cleared), Toast.LENGTH_SHORT).show()
                        navController.navigate("view_movies") {
                            popUpTo("movies") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄 " + stringResource(R.string.clear_filters))
                }

                Button(onClick = { showFieldMenu = true }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.sort_button))
                }

                DropdownMenu(expanded = showFieldMenu, onDismissRequest = { showFieldMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_title)) }, onClick = {
                        sortField = "title"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_director)) }, onClick = {
                        sortField = "director"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_publish_date)) }, onClick = {
                        sortField = "publishDate"; showFieldMenu = false; showDirectionMenu = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_by_rating)) }, onClick = {
                        sortField = "rating"; showFieldMenu = false; showDirectionMenu = true
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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(movies) { (id, movie) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.movie_title_label, movie.title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.movie_director_label, movie.director))
                            Text(stringResource(R.string.movie_genre_label, movie.genre))
                            Text(stringResource(R.string.movie_publish_date_label, movie.publishDate))

                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("edit_movie/$id")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                IconButton(onClick = {
                                    navController.navigate("details_movie/$id")
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = "Details")
                                }

                                IconButton(onClick = {
                                    movieToDelete = id to movie
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

    movieToDelete?.let { (movieId, movie) ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseFirestore.getInstance().collection("movies").document(movieId)
                        .delete()
                        .addOnSuccessListener {
                            moviesRaw = moviesRaw.filterNot { it.first == movieId }
                        }
                    movieToDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(movie.title) }
        )
    }
}

fun movieSortKey(movie: Movie, field: String): Comparable<*> {
    return when (field) {
        "title" -> movie.title.lowercase()
        "genre" -> movie.genre.lowercase()
        "director" -> movie.director.lowercase()
        "publishDate" -> movie.publishDate
        "rating" -> movie.rating.toIntOrNull() ?: 0
        else -> movie.title.lowercase()
    }
}
