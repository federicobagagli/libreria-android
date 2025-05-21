package com.federico.mylibrary.book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.federico.mylibrary.ui.PieChartSection
import com.federico.mylibrary.ui.Legend
import com.federico.mylibrary.util.stringResourceByName
import androidx.compose.ui.platform.LocalContext

@Composable
fun LibraryPieChartsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var genreCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var readingStatusCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var formatCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var languageCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var ratingCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val genres = snapshot.documents.mapNotNull { it.getString("genre")?.lowercase() }
            genreCounts = genres.groupingBy { it }.eachCount()

            val validReadingKeys = listOf("not_started", "reading", "completed")
            val statuses = snapshot.documents.mapNotNull {
                val raw = it.getString("readingStatus")?.lowercase()?.trim()
                when {
                    raw in validReadingKeys -> raw
                    raw == "non iniziato" -> "not_started"
                    raw == "in lettura" -> "reading"
                    raw == "completato" -> "completed"
                    else -> null
                }
            }
            val rawReadingStatusCounts = statuses.groupingBy { it }.eachCount()
            readingStatusCounts = rawReadingStatusCounts.mapKeys { (key) ->
                stringResourceByName("status_${key}", context)
            }

            val validFormatKeys = listOf("physical", "ebook", "audio")
            val formats = snapshot.documents.mapNotNull {
                val raw = it.getString("format")?.lowercase()?.trim()
                when {
                    raw in validFormatKeys -> raw
                    raw == "cartaceo" || raw == "fisico" -> "physical"
                    raw == "audiolibro" -> "audio"
                    else -> null
                }
            }
            val rawFormatCounts = formats.groupingBy { it }.eachCount()
            formatCounts = rawFormatCounts.mapKeys { (key) ->
                stringResourceByName("format_${key}", context)
            }

            val languages = snapshot.documents.mapNotNull { it.getString("language")?.lowercase() }
            languageCounts = languages.groupingBy { it }.eachCount()

            val ratings = snapshot.documents.mapNotNull {
                val raw = it.get("rating")
                when (raw) {
                    is String -> raw.trim()
                    is Number -> raw.toInt().toString()
                    else -> ""
                }
            }
            ratingCounts = ratings.map { if (it.isBlank()) "" else it }
                .groupingBy { it }.eachCount()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.pie_charts_title), style = MaterialTheme.typography.headlineMedium)

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

        if (formatCounts.isNotEmpty()) {
            Text(stringResource(R.string.format_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(formatCounts)
            Legend(formatCounts, defaultLabel = stringResource(R.string.unknown_format))
        }

        if (languageCounts.isNotEmpty()) {
            Text(stringResource(R.string.language_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(languageCounts)
            Legend(languageCounts, defaultLabel = stringResource(R.string.unknown_language))
        }

        if (ratingCounts.isNotEmpty()) {
            Text(stringResource(R.string.rating_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(ratingCounts)
            Legend(ratingCounts, defaultLabel = stringResource(R.string.no_rating))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}

