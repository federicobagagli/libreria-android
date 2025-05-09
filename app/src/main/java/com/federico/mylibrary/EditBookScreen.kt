package com.federico.mylibrary

import android.widget.Toast
import androidx.compose.foundation.layout.*
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

@Composable
fun EditBookScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val bookId = backStackEntry.arguments?.getString("bookId") ?: return
    val db = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookId) {
        val doc = db.collection("books").document(bookId).get().await()
        val book = doc.toObject(Book::class.java)
        if (book != null) {
            title = book.title
            author = book.author
            genre = book.genre
            publishDate = book.publishDate
        }
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.author)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text(stringResource(R.string.genre)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = publishDate, onValueChange = { publishDate = it }, label = { Text(stringResource(R.string.publish_date)) }, modifier = Modifier.fillMaxWidth())

            val missingTitle = stringResource(R.string.missing_title)
            val bookUpdated = stringResource(R.string.book_updated)
            val errorPrefix = stringResource(R.string.error_prefix)
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedBook = mapOf(
                        "title" to title,
                        "author" to author,
                        "genre" to genre,
                        "publishDate" to publishDate
                    )

                    db.collection("books").document(bookId)
                        .update(updatedBook)
                        .addOnSuccessListener {
                            Toast.makeText(context, bookUpdated, Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "errorPrefix ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_book))
            }
        }
    }
}
