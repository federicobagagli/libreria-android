package com.federico.mylibrary.movie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.ui.Legend
import com.federico.mylibrary.ui.PieChartSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun MoviePieChartsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var genreDistribution by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var formatDistribution by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("movies")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val genres = snapshot.documents.mapNotNull { it.getString("genre")?.lowercase() }
            genreDistribution = genres.groupingBy { it }.eachCount()

            val formats = snapshot.documents.mapNotNull { it.getString("format")?.lowercase() }
            formatDistribution = formats.groupingBy { it }.eachCount()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(R.string.genre_distribution), style = MaterialTheme.typography.titleLarge)
        PieChartSection(data = genreDistribution)
        Legend(data = genreDistribution)

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.format_distribution), style = MaterialTheme.typography.titleLarge)
        PieChartSection(data = formatDistribution)
        Legend(data = formatDistribution)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}
