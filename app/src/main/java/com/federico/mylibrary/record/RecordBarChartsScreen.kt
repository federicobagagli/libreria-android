package com.federico.mylibrary.record

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
fun RecordBarChartsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var monthlyCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val months = snapshot.documents.mapNotNull {
                it.getString("addedDate")?.takeIf { d -> d.length >= 7 }?.substring(0, 7)
            }
            monthlyCounts = months.groupingBy { it }.eachCount().toSortedMap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = stringResource(R.string.bar_record_charts_title), style = MaterialTheme.typography.headlineMedium)

        if (monthlyCounts.isNotEmpty()) {
            BarChart(monthlyCounts)
        } else {
            Text(stringResource(R.string.no_data_available))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun BarChart(data: Map<String, Int>) {
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
                    drawRect(color = Color(0xFF64B5F6))
                }
                Text(" $count", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
