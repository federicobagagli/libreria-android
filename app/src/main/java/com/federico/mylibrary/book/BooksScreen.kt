package com.federico.mylibrary.book

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import com.federico.mylibrary.R
import com.federico.mylibrary.export.BookExportItem
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.model.Book
import com.federico.mylibrary.viewmodel.LibraryFilterViewModel
import com.federico.mylibrary.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.federico.mylibrary.util.PremiumBlocker


@Composable
fun BooksScreen(
    navController: NavController,
    exportViewModel: ExportViewModel,
    filterViewModel: LibraryFilterViewModel,
    userViewModel: UserViewModel
) {
    val isPremium by userViewModel.isPremium.collectAsState()
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val filters by filterViewModel.filterState.collectAsState()
    val context = LocalContext.current

    val showAll = navController.currentBackStackEntry?.arguments?.getString("showAll") == "true"

    var booksRaw by remember { mutableStateOf<List<Pair<String, Book>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var bookToDelete by remember { mutableStateOf<Pair<String, Book>?>(null) }
    var expandedCoverUrl by remember { mutableStateOf<String?>(null) }

    // 🔽 Stato per ordinamento
    var sortField by remember { mutableStateOf("title") }
    var sortDirection by remember { mutableStateOf("asc") }
    var showFieldMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }


    val books by remember(sortField, sortDirection, booksRaw) {
        derivedStateOf {
            val sorted = booksRaw.sortedWith(compareBy { bookSortKey(it.second, sortField) })
            if (sortDirection == "desc") sorted.reversed() else sorted
        }
    }


    LaunchedEffect(userId, filters) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            booksRaw = snapshot.documents.mapNotNull {
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
                        (filters.rating.isBlank() || book.rating == filters.rating) &&
                        match(book.notes, filters.notes) &&
                        match(book.coverUrl, filters.coverUrl) &&
                        match(book.location, filters.location)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val smallButtonModifier = Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                val smallTextStyle = MaterialTheme.typography.labelSmall
                PremiumBlocker(
                    isPremium = isPremium,
                    modifier = smallButtonModifier,
                    onClickAllowed = {
                        exportViewModel.setExportData(
                            items = books.map { (_, book) ->
                                BookExportItem(
                                    title = book.title,
                                    author = book.author,
                                    publisher = book.publisher,
                                    genre = book.genre,
                                    language = book.language,
                                    description = book.description,
                                    pageCount = book.pageCount,
                                    format = book.format,
                                    readingStatus = book.readingStatus,
                                    addedDate = book.addedDate,
                                    rating = book.rating,
                                    notes = book.notes,
                                    coverUrl = book.coverUrl,
                                    publishDate = book.publishDate,
                                    location = book.location
                                )
                            },
                            fileName = "library_export.csv"
                        )
                        navController.navigate("exportView")
                    }
                ) {
                    Text("📤 " + stringResource(R.string.export_title_book),style = smallTextStyle)
                }

                Button(
                    onClick = {
                        navController.navigate("view_library")
                    },
                    modifier = smallButtonModifier,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(stringResource(R.string.filter),style = smallTextStyle) // oppure "🔍 Filtra" se vuoi aggiungere un'icona
                }

                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        Toast.makeText(
                            context,
                            context.getString(R.string.filters_cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate("view_library") {
                            popUpTo("books") { inclusive = true }
                        }
                    },
                    modifier = smallButtonModifier,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄 " + stringResource(R.string.clear_filters),style = smallTextStyle)
                }

                Button(
                    onClick = { showFieldMenu = true },
                    modifier = smallButtonModifier
                ) {
                    Text(stringResource(R.string.sort_button))
                }

                DropdownMenu(
                    expanded = showFieldMenu,
                    onDismissRequest = { showFieldMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_title)) },
                        onClick = {
                            sortField = "title"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_author)) },
                        onClick = {
                            sortField = "author"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_added_date)) },
                        onClick = {
                            sortField = "addedDate"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_rating)) },
                        onClick = {
                            sortField = "rating"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_genre)) },
                        onClick = {
                            sortField = "genre"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_publisher)) },
                        onClick = {
                            sortField = "publisher"; showFieldMenu = false; showDirectionMenu = true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_publish_date)) },
                        onClick = {
                            sortField = "publishDate"; showFieldMenu = false; showDirectionMenu =
                            true
                        })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_location)) },
                        onClick = {
                            sortField = "location"; showFieldMenu = false; showDirectionMenu = true
                        })
                }

                DropdownMenu(
                    expanded = showDirectionMenu,
                    onDismissRequest = { showDirectionMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_asc)) }, onClick = {
                        sortDirection = "asc"; showDirectionMenu = false
                    })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_desc)) },
                        onClick = {
                            sortDirection = "desc"; showDirectionMenu = false
                        })
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
                            if (book.coverUrl.isNotBlank()) {
                                val imageUrl = book.coverUrl.replace("http://", "https://")
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = stringResource(R.string.book_cover_url),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { expandedCoverUrl = imageUrl }
                                )
                            }


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
                            booksRaw = booksRaw.filterNot { it.first == bookId }
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
    if (expandedCoverUrl != null) {
        AlertDialog(
            onDismissRequest = { expandedCoverUrl = null },
            confirmButton = {
                TextButton(onClick = { expandedCoverUrl = null }) {
                    Text(stringResource(R.string.close))
                }
            },
            text = {
                AsyncImage(
                    model = expandedCoverUrl,
                    contentDescription = stringResource(R.string.book_cover_url),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                )
            }
        )
    }

}

fun bookSortKey(book: Book, field: String): Comparable<*> {
    return when (field) {
        "title" -> book.title.lowercase()
        "author" -> book.author.lowercase()
        "addedDate" -> book.addedDate
        "rating" -> book.rating.toIntOrNull() ?: 0
        "genre" -> book.genre.lowercase()
        "publisher" -> book.publisher.lowercase()
        "publishDate" -> book.publishDate
        else -> book.title.lowercase()
    }
}

