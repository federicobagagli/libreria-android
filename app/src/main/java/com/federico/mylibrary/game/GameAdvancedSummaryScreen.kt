package com.federico.mylibrary.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun GameAdvancedSummaryScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.advanced_summary_title), style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { navController.navigate("game_pie_charts") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_pie_charts))
        }

        Button(
            onClick = { navController.navigate("game_bar_charts") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_monthly_bar_chart))
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.back))
        }
    }
}
