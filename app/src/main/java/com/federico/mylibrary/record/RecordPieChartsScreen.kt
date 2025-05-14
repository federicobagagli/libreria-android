package com.federico.mylibrary.record

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
import com.federico.mylibrary.ui.PieChartSection
import com.federico.mylibrary.ui.Legend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RecordPieChartsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var genreCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var formatCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var typeCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var languageCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            genreCounts = snapshot.documents.mapNotNull { it.getString("genre")?.lowercase() }
                .groupingBy { it }.eachCount()

            formatCounts = snapshot.documents.mapNotNull { it.getString("format")?.lowercase() }
                .groupingBy { it }.eachCount()

            typeCounts = snapshot.documents.mapNotNull { it.getString("type")?.lowercase() }
                .groupingBy { it }.eachCount()

            languageCounts = snapshot.documents.mapNotNull { it.getString("language")?.lowercase() }
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

        if (formatCounts.isNotEmpty()) {
            Text(stringResource(R.string.format_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(formatCounts)
            Legend(formatCounts, defaultLabel = stringResource(R.string.unknown_format))
        }

        if (typeCounts.isNotEmpty()) {
            Text(stringResource(R.string.record_type_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(typeCounts)
            Legend(typeCounts)
        }

        if (languageCounts.isNotEmpty()) {
            Text(stringResource(R.string.language_distribution), style = MaterialTheme.typography.titleMedium)
            PieChartSection(languageCounts)
            Legend(languageCounts, defaultLabel = stringResource(R.string.unknown_language))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}
