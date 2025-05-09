package com.federico.mylibrary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

@Composable
fun BooksScreen() {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            books = snapshot.documents.mapNotNull { it.toObject<Book>() }
        }
        isLoading = false
    }

    Column(modifier = Modifier.padding(16.dp)) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            books.isEmpty() -> {
                Text(stringResource(R.string.no_books_found))
            }
            else -> {
                books.forEach { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.book_title_label, book.title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.book_author_label, book.author))
                            Text(stringResource(R.string.book_genre_label, book.genre))
                            Text(stringResource(R.string.book_publish_date_label, book.publishDate))
                        }
                    }
                }
            }
        }
    }
}
