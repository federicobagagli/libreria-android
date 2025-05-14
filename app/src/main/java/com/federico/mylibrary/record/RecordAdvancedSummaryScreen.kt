
package com.federico.mylibrary.record

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RecordAdvancedSummaryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading by remember { mutableStateOf(true) }

    var formatCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var typeCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var languageCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var genreCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val scrollState = rememberScrollState()
    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { it.data }

            formatCounts = records.mapNotNull { it["format"]?.toString() }
                .groupingBy { it }.eachCount()

            typeCounts = records.mapNotNull { it["type"]?.toString() }
                .groupingBy { it }.eachCount()

            languageCounts = records.mapNotNull { it["language"]?.toString() }
                .groupingBy { it }.eachCount()

            genreCounts = records.mapNotNull { it["genre"]?.toString() }
                .groupingBy { it }.eachCount()
        }
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.advanced_summary),
                style = MaterialTheme.typography.headlineSmall
            )

            SummaryCategory(
                title = stringResource(R.string.record_format_distribution),
                data = formatCounts
            )

            SummaryCategory(
                title = stringResource(R.string.record_type_distribution),
                data = typeCounts
            )

            SummaryCategory(
                title = stringResource(R.string.record_language_distribution),
                data = languageCounts
            )

            SummaryCategory(
                title = stringResource(R.string.record_genre_distribution),
                data = genreCounts
            )

            Button(
                onClick = { navController.navigate("record_pie_charts") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_pie_charts))
            }

            Button(
                onClick = { navController.navigate("record_bar_charts") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.view_monthly_bar_chart))
            }

        }
    }
}

@Composable
fun SummaryCategory(title: String, data: Map<String, Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        data.forEach { (key, value) ->
            val displayKey = if (key.isBlank()) stringResource(R.string.not_defined) else key
            Text(text = "$displayKey: $value")
        }
    }
}
