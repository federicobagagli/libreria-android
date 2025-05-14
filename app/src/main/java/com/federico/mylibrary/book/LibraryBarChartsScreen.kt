package com.federico.mylibrary.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun LibraryBarChartsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var addedCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var readCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var totalAdded by remember { mutableStateOf(0) }
    var totalRead by remember { mutableStateOf(0) }
    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val addedMonths = snapshot.documents.mapNotNull {
                it.getString("addedDate")?.takeIf { d -> d.length >= 7 }?.substring(0, 7)
            }
            addedCounts = addedMonths.groupingBy { it }.eachCount().toSortedMap()

            val readMonths = snapshot.documents.mapNotNull {
                it.getString("readDate")?.takeIf { d -> d.length >= 7 }?.substring(0, 7)
            }
            readCounts = readMonths.groupingBy { it }.eachCount().toSortedMap()

            totalAdded = addedCounts.values.sum()
            totalRead = readCounts.values.sum()
        }
    }


    println("Totale aggiunti: $totalAdded, letti: $totalRead")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.bar_charts_title), style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp) // Spaziatura minima
            ) {
                println("Totale aggiunti: $totalAdded, letti: $totalRead")
                Text(
                    text = stringResource(R.string.total_books_added, totalAdded),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.total_books_read, totalRead),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (addedCounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.books_added), style = MaterialTheme.typography.titleMedium)
            SingleBarChart(data = addedCounts, color = Color(0xFF64B5F6))
        }

        if (readCounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.books_read), style = MaterialTheme.typography.titleMedium)
            SingleBarChart(data = readCounts, color = Color(0xFF81C784))
        }

        if (addedCounts.isEmpty() && readCounts.isEmpty()) {
            Text(stringResource(R.string.no_data_available))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun SingleBarChart(data: Map<String, Int>, color: Color) {
    val maxCount = (data.values.maxOrNull() ?: 1).coerceAtLeast(1)
    val barWidth = 40.dp
    val spacing = 16.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        data.forEach { (month, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(month, modifier = Modifier.width(60.dp))
                Canvas(modifier = Modifier
                    .height(20.dp)
                    .width(barWidth * count / maxCount + spacing)) {
                    drawRect(color = color)
                }
                Text(" $count", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
