package com.federico.mylibrary.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.viewmodel.GameFilterState
import com.federico.mylibrary.viewmodel.GameFilterViewModel

@Composable
fun ViewGamesScreen(navController: NavController, filterViewModel: GameFilterViewModel) {
    var filters by remember { mutableStateOf(GameFilterState()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pulsanti in alto
        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        navController.navigate("games")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.view_games))
                }

                Button(
                    onClick = {
                        filterViewModel.updateFilters(filters)
                        navController.navigate("games")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.view_by_criteria))
                }

                Button(
                    onClick = { navController.navigate("add_game") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.add_game), color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.select_filters_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Filtri
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fun Modifier.full() = this.fillMaxWidth()
            OutlinedTextField(filters.title, { filters = filters.copy(title = it) }, label = { Text(stringResource(R.string.game_title)) }, modifier = Modifier.full())
            OutlinedTextField(filters.type, { filters = filters.copy(type = it) }, label = { Text(stringResource(R.string.game_type)) }, modifier = Modifier.full())
            OutlinedTextField(filters.platform, { filters = filters.copy(platform = it) }, label = { Text(stringResource(R.string.game_platform)) }, modifier = Modifier.full())
            OutlinedTextField(filters.publisher, { filters = filters.copy(publisher = it) }, label = { Text(stringResource(R.string.game_publisher)) }, modifier = Modifier.full())
            OutlinedTextField(filters.releaseDate, { filters = filters.copy(releaseDate = it) }, label = { Text(stringResource(R.string.game_release_date)) }, modifier = Modifier.full())
            OutlinedTextField(filters.genre, { filters = filters.copy(genre = it) }, label = { Text(stringResource(R.string.game_genre)) }, modifier = Modifier.full())
            OutlinedTextField(filters.language, { filters = filters.copy(language = it) }, label = { Text(stringResource(R.string.game_language)) }, modifier = Modifier.full())
            OutlinedTextField(filters.description, { filters = filters.copy(description = it) }, label = { Text(stringResource(R.string.game_description)) }, modifier = Modifier.full())
            OutlinedTextField(filters.minPlayers, { filters = filters.copy(minPlayers = it) }, label = { Text(stringResource(R.string.game_min_players)) }, modifier = Modifier.full())
            OutlinedTextField(filters.maxPlayers, { filters = filters.copy(maxPlayers = it) }, label = { Text(stringResource(R.string.game_max_players)) }, modifier = Modifier.full())
            OutlinedTextField(filters.durationMinutes, { filters = filters.copy(durationMinutes = it) }, label = { Text(stringResource(R.string.game_duration_minutes)) }, modifier = Modifier.full())
            OutlinedTextField(filters.rating, { filters = filters.copy(rating = it) }, label = { Text(stringResource(R.string.game_rating)) }, modifier = Modifier.full())
            OutlinedTextField(filters.notes, { filters = filters.copy(notes = it) }, label = { Text(stringResource(R.string.game_notes)) }, modifier = Modifier.full())
            OutlinedTextField(filters.location, { filters = filters.copy(location = it) }, label = { Text(stringResource(R.string.game_location)) }, modifier = Modifier.full())
            OutlinedTextField(filters.coverUrl, { filters = filters.copy(coverUrl = it) }, label = { Text(stringResource(R.string.game_cover_url)) }, modifier = Modifier.full())
        }
    }
}
