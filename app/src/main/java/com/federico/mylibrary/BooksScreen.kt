package com.federico.mylibrary

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.federico.mylibrary.export.BookExportItem
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.model.Book
import com.federico.mylibrary.viewmodel.LibraryFilterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

@Composable
fun BooksScreen(
    navController: NavController,
    exportViewModel: ExportViewModel,
    filterViewModel: LibraryFilterViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val filters by filterViewModel.filterState.collectAsState()
    val context = LocalContext.current

    var books by remember { mutableStateOf<List<Pair<String, Book>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var bookToDelete by remember { mutableStateOf<Pair<String, Book>?>(null) }

    LaunchedEffect(userId, filters) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            books = snapshot.documents.mapNotNull {
                val book = it.toObject<Book>()
                if (book != null) it.id to book else null
            }.filter { (_, book) ->
                fun match(input: String, filter: String) =
                    filter.isBlank() || input.contains(filter, ignoreCase = true)

                match(book.title, filters.title) &&
                        match(book.author, filters.author) &&
                        match(book.publisher, filters.publisher) &&
                        match(book.genre, filters.genre) &&
                        match(book.language, filters.language) &&
                        match(book.publishDate, filters.publishDate) &&
                        match(book.description, filters.description) &&
                        (filters.pageCount.isBlank() || book.pageCount.toString() == filters.pageCount) &&
                        match(book.format, filters.format) &&
                        match(book.readingStatus, filters.readingStatus) &&
                        (filters.rating.isBlank() || book.rating.toString() == filters.rating) &&
                        match(book.notes, filters.notes) &&
                        match(book.coverUrl, filters.coverUrl)
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else if (books.isEmpty()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.no_books_found))
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        exportViewModel.setExportData(
                            items = books.map { (_, book) ->
                                BookExportItem(book.title, book.author, book.genre, book.publishDate)
                            },
                            fileName = "library_export.csv"
                        )
                        navController.navigate("exportView")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 " + stringResource(R.string.export_title_book))
                }

                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        Toast.makeText(context, context.getString(R.string.filters_cleared), Toast.LENGTH_SHORT).show()
                        navController.navigate("view_library") {
                            popUpTo("books") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄 " + stringResource(R.string.clear_filters))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(books) { (id, book) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.book_title_label, book.title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(stringResource(R.string.book_author_label, book.author))
                            Text(stringResource(R.string.book_genre_label, book.genre))
                            Text(stringResource(R.string.book_publish_date_label, book.publishDate))

                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("edit_book/$id")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                IconButton(onClick = {
                                    navController.navigate("details_book/$id")
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = "Details")
                                }

                                IconButton(onClick = {
                                    bookToDelete = id to book
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

    bookToDelete?.let { (bookId, book) ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseFirestore.getInstance().collection("books").document(bookId)
                        .delete()
                        .addOnSuccessListener {
                            books = books.filterNot { it.first == bookId }
                        }
                    bookToDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text("\"${book.title}\"") }
        )
    }
}
