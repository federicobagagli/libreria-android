package com.federico.mylibrary.record

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
import com.federico.mylibrary.viewmodel.RecordFilterState
import com.federico.mylibrary.viewmodel.RecordFilterViewModel

@Composable
fun ViewRecordsScreen(navController: NavController, filterViewModel: RecordFilterViewModel) {
    var filters by remember { mutableStateOf(RecordFilterState()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra pulsanti
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
                        navController.navigate("records")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.view_all_records))
                }

                Button(
                    onClick = {
                        filterViewModel.updateFilters(filters)
                        navController.navigate("records")
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.view_by_criteria))
                }

                Button(
                    onClick = { navController.navigate("add_record") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.add_record), color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.select_filters_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Campi filtro
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fun Modifier.full() = this.fillMaxWidth()

            OutlinedTextField(filters.title, { filters = filters.copy(title = it) }, label = { Text("Titolo") }, modifier = Modifier.full())
            OutlinedTextField(filters.artist, { filters = filters.copy(artist = it) }, label = { Text("Artista") }, modifier = Modifier.full())
            OutlinedTextField(filters.genre, { filters = filters.copy(genre = it) }, label = { Text("Genere") }, modifier = Modifier.full())
            OutlinedTextField(filters.year, { filters = filters.copy(year = it) }, label = { Text("Anno") }, modifier = Modifier.full())
            OutlinedTextField(filters.type, { filters = filters.copy(type = it) }, label = { Text("Tipo (Brano o Album)") }, modifier = Modifier.full())
            OutlinedTextField(filters.format, { filters = filters.copy(format = it) }, label = { Text("Formato") }, modifier = Modifier.full())
        }
    }
}
