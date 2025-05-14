package com.federico.mylibrary.movie

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun MovieRoomScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.movie_room_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { navController.navigate("view_movies") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_movies), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("add_movie") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_movie), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("movie_summary") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.summary), fontSize = 18.sp)
        }
    }
}
