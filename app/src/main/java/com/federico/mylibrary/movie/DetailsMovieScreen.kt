package com.federico.mylibrary.movie

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
import com.federico.mylibrary.model.Movie
import com.federico.mylibrary.util.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun DetailsMovieScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val movieId = backStackEntry.arguments?.getString("movieId") ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var movie by remember { mutableStateOf<Movie?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        val doc = db.collection("movies").document(movieId).get().await()
        movie = doc.toObject(Movie::class.java)
        Logger.d("COVER_URL", "URL = ${movie?.coverUrl}")
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        movie?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.title) + ": " + it.title)
                Text(stringResource(R.string.original_title) + ": " + it.originalTitle)
                Text(stringResource(R.string.director) + ": " + it.director)
                Text(stringResource(R.string.cast) + ": " + it.cast)
                Text(stringResource(R.string.production_company) + ": " + it.productionCompany)
                Text(stringResource(R.string.genre) + ": " + it.genre)
                Text(stringResource(R.string.book_language) + ": " + it.language)
                Text(stringResource(R.string.publish_date) + ": " + it.publishDate)
                Text(stringResource(R.string.book_description) + ": " + it.description)
                Text(stringResource(R.string.duration_minutes) + ": ${it.duration}")
                Text(stringResource(R.string.format) + ": " + it.format)
                Text(stringResource(R.string.book_rating) + ": ${it.rating}")
                Text(stringResource(R.string.book_notes) + ": " + it.notes)
                Text(stringResource(R.string.book_cover_url) + ": " + it.coverUrl)
                Text(stringResource(R.string.book_location) + ": " + it.location)

                if (it.coverUrl.isNotBlank()) {
                    val coverUrl = it.coverUrl.replace("http://", "https://")
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = stringResource(R.string.book_cover_url),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(width = 100.dp, height = 150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.back))
                }
            }
        } ?: run {
            Text(text = stringResource(R.string.movie_not_found), modifier = Modifier.padding(16.dp))
        }
    }
}
