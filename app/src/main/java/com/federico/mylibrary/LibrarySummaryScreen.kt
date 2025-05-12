package com.federico.mylibrary


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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun LibrarySummaryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var totalBooks by remember { mutableStateOf(0) }
    var readBooks by remember { mutableStateOf(0) }
    var mostCommonGenre by remember { mutableStateOf<String?>(null) }
    var lastAddedBook by remember { mutableStateOf<String?>(null) }
    var genreCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var readingStatusCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            totalBooks = snapshot.size()
            readBooks = snapshot.documents.count {
                it.getString("readingStatus")?.lowercase() == context.getString(R.string.status_completed).lowercase()
            }

            val genres = snapshot.documents.mapNotNull { it.getString("genre")?.lowercase() }
            genreCounts = genres.groupingBy { it }.eachCount()
            mostCommonGenre = genreCounts.maxByOrNull { it.value }?.key

            lastAddedBook = snapshot.documents
                .maxByOrNull { it.getString("addedDate") ?: "" }
                ?.getString("title")

            val statuses = snapshot.documents.mapNotNull { it.getString("readingStatus") }
            readingStatusCounts = statuses.groupingBy { it }.eachCount()
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
                Text(text = stringResource(R.string.total_books, totalBooks))
                Text(text = stringResource(R.string.books_read, readBooks))
                mostCommonGenre?.let {
                    Text(text = stringResource(R.string.most_common_genre, it))
                }
                lastAddedBook?.let {
                    Text(text = stringResource(R.string.last_added_book, it))
                }
            }
        }

        Button(onClick = { navController.navigate("library_advanced_summary") }) {
            Text(stringResource(R.string.advanced_summary_title))
        }


        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}


