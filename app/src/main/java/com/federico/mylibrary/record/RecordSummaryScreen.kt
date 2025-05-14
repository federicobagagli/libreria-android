
package com.federico.mylibrary.record

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun RecordSummaryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var totalRecords by remember { mutableStateOf(0) }
    var mostFrequentType by remember { mutableStateOf<String?>(null) }
    var mostFrequentGenre by remember { mutableStateOf<String?>(null) }
    var latestRecordTitle by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { it.data }

            totalRecords = records.size

            mostFrequentType = records
                .mapNotNull { it["type"]?.toString() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            mostFrequentGenre = records
                .mapNotNull { it["genre"]?.toString() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            latestRecordTitle = records
                .sortedByDescending { it["addedDate"]?.toString() }
                .firstOrNull()
                ?.get("title")?.toString()
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.summary),
                style = MaterialTheme.typography.headlineSmall
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_records, totalRecords),
                    style = MaterialTheme.typography.titleLarge
                )

                if (mostFrequentGenre != null) {
                    Text(
                        text = stringResource(R.string.most_common_record_genre, mostFrequentGenre!!),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (latestRecordTitle != null) {
                    Text(
                        text = stringResource(R.string.latest_record_added, latestRecordTitle!!),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Button(
                onClick = { navController.navigate("record_advanced_summary") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.go_to_advanced_summary))
            }
        }
    }
}
