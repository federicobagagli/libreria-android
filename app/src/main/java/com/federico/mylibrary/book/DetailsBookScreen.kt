package com.federico.mylibrary.book

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
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage

@Composable
fun DetailsBookScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val bookId = backStackEntry.arguments?.getString("bookId") ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookId) {
        val doc = db.collection("books").document(bookId).get().await()
        book = doc.toObject(Book::class.java)
        Log.d("COVER_URL", "URL = ${book?.coverUrl}")
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        book?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.title) + ": " + it.title)
                Text(text = stringResource(R.string.author) + ": " + it.author)
                Text(text = stringResource(R.string.book_publisher) + ": " + it.publisher)
                Text(text = stringResource(R.string.genre) + ": " + it.genre)
                Text(text = stringResource(R.string.book_language) + ": " + it.language)
                Text(text = stringResource(R.string.publish_date) + ": " + it.publishDate)
                Text(text = stringResource(R.string.book_description) + ": " + it.description)
                Text(text = stringResource(R.string.book_page_count) + ": ${it.pageCount}")
                Text(text = stringResource(R.string.format) + ": " + it.format)
                Text(text = stringResource(R.string.reading_status) + ": " + it.readingStatus)
                Text(text = stringResource(R.string.book_added_date) + ": " + it.addedDate)
                Text(text = stringResource(R.string.book_rating) + ": ${it.rating}")
                Text(text = stringResource(R.string.book_notes) + ": " + it.notes)
                Text(text = stringResource(R.string.book_cover_url) + ": " + it.coverUrl)
                Text(text = stringResource(R.string.book_location) + ": " + it.location)
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
            Text(text = stringResource(R.string.book_not_found), modifier = Modifier.padding(16.dp))
        }
    }
}
