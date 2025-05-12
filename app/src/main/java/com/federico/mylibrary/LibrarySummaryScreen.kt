package com.federico.mylibrary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

            val genres = snapshot.documents.mapNotNull { it.getString("genre") }
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

        if (genreCounts.isNotEmpty()) {
            Text(stringResource(R.string.genre_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(genreCounts)
            Legend(genreCounts)
        }

        if (readingStatusCounts.isNotEmpty()) {
            Text(stringResource(R.string.reading_status_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(readingStatusCounts)
            Legend(readingStatusCounts)
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun PieChartSection(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    Canvas(modifier = Modifier
        .height(250.dp)
        .fillMaxWidth()
    ) {
        var startAngle = 0f
        val radius = size.minDimension / 2.2f
        val center = this.center

        data.entries.forEachIndexed { index, (label, count) ->
            val sweep = 360f * (count / total)
            drawArc(
                color = themedColor(index),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = center.copy(
                    x = center.x - radius,
                    y = center.y - radius
                )
            )
            startAngle += sweep
        }
    }
}

@Composable
fun Legend(data: Map<String, Int>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        data.keys.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(themedColor(index), shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(label)
            }
        }
    }
}

val softColors = listOf(
    Color(0xFF81D4FA), // Light Blue
    Color(0xFFA5D6A7), // Light Green
    Color(0xFFFFCC80), // Orange
    Color(0xFFCE93D8), // Purple
    Color(0xFFFFAB91), // Coral
    Color(0xFFB0BEC5), // Gray Blue
    Color(0xFFE6EE9C), // Lime
    Color(0xFFFFF59D), // Yellow
    Color(0xFFB39DDB), // Lavender
    Color(0xFF80CBC4)  // Teal
)

fun themedColor(index: Int): Color {
    return softColors[index % softColors.size]
}
