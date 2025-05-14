package com.federico.mylibrary.movie

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun MovieAdvancedSummaryScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.advanced_summary_title), style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { navController.navigate("movie_pie_charts") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pie_charts_button))
        }

        Button(
            onClick = { navController.navigate("movie_bar_charts") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.bar_charts_button))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.back))
        }
    }
}
