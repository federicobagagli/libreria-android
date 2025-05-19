
package com.federico.mylibrary.record

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.viewmodel.RecordFilterViewModel
import com.federico.mylibrary.viewmodel.RecordFilterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.export.RecordExportItem

@Composable
fun RecordsScreen(
    navController: NavController,
    exportViewModel: ExportViewModel,
    filterViewModel: RecordFilterViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val filters by filterViewModel.filterState.collectAsState()
    val context = LocalContext.current

    var recordsRaw by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var recordToDelete by remember { mutableStateOf<Pair<String, Map<String, Any>>?>(null) }

    var sortField by remember { mutableStateOf("title") }
    var sortDirection by remember { mutableStateOf("asc") }
    var showFieldMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }

    val records by remember(sortField, sortDirection, recordsRaw) {
        derivedStateOf {
            val sorted = recordsRaw.sortedWith(compareBy { it.second[sortField].toString().lowercase() })
            if (sortDirection == "desc") sorted.reversed() else sorted
        }
    }

    LaunchedEffect(userId, filters) {
        if (userId != null) {
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            recordsRaw = snapshot.documents.mapNotNull {
                val data = it.data
                if (data != null) it.id to data else null
            }.filter { (_, data) ->
                fun match(input: Any?, filter: String): Boolean {
                    val str = input?.toString()?.lowercase() ?: ""
                    return filter.isBlank() || str.contains(filter.lowercase())
                }

                match(data["title"], filters.title) &&
                        match(data["artist"], filters.artist) &&
                        match(data["genre"], filters.genre) &&
                        match(data["year"], filters.year) &&
                        match(data["type"], filters.type) &&
                        match(data["format"], filters.format)
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else if (records.isEmpty()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.no_records_found))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val smallButtonModifier = Modifier
                .weight(1f)
                .heightIn(min = 36.dp)
            val smallTextStyle = MaterialTheme.typography.labelSmall
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        exportViewModel.setExportData(
                            items = records.map { (_, record) ->
                                RecordExportItem(
                                    title = record["title"]?.toString() ?: "",
                                    artist = record["artist"]?.toString() ?: "",
                                    format = record["format"]?.toString() ?: "",
                                    year = record["year"]?.toString() ?: "",
                                    genre = record["genre"]?.toString() ?: "",
                                    physicalSupport = record["physicalSupport"] as? Boolean ?: false,
                                    type = record["type"]?.toString() ?: "",
                                    trackNumber = record["trackNumber"]?.toString() ?: "",
                                    album = record["album"]?.toString() ?: "",
                                    duration = record["duration"]?.toString() ?: "",
                                    label = record["label"]?.toString() ?: "",
                                    soloists = record["soloists"]?.toString() ?: "",
                                    tracklist = (record["tracklist"] as? List<*>)?.joinToString(" | ") ?: "",
                                    totalTracks = record["totalTracks"]?.toString() ?: "",
                                    multiAlbum = record["multiAlbum"] as? Boolean ?: false,
                                    language = record["language"]?.toString() ?: "",
                                    description = record["description"]?.toString() ?: "",
                                    rating = record["rating"]?.toString() ?: "",
                                    notes = record["notes"]?.toString() ?: "",
                                    coverUrl = record["coverUrl"]?.toString() ?: "",
                                    addedDate = record["addedDate"]?.toString() ?: "",
                                    location = record["location"]?.toString() ?: ""
                                )
                            },
                            fileName = "records_export.csv"
                        )
                        navController.navigate("exportView")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 " + stringResource(R.string.export_title_record))
                }

                Button(
                    onClick = {
                        navController.navigate("view_records")
                    },
                    modifier = smallButtonModifier,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(stringResource(R.string.filter),style = smallTextStyle) // oppure "🔍 Filtra" se vuoi aggiungere un'icona
                }

                Button(
                    onClick = {
                        filterViewModel.clearFilters()
                        Toast.makeText(context, context.getString(R.string.filters_cleared), Toast.LENGTH_SHORT).show()
                        navController.navigate("view_records") {
                            popUpTo("records") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔄 " + stringResource(R.string.clear_filters))
                }

                Button(
                    onClick = { showFieldMenu = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.sort_button))
                }

                DropdownMenu(expanded = showFieldMenu, onDismissRequest = { showFieldMenu = false }) {
                    listOf("title", "artist", "genre", "year", "format").forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(context.resources.getIdentifier("sort_by_$it", "string", context.packageName))) },
                            onClick = {
                                sortField = it
                                showFieldMenu = false
                                showDirectionMenu = true
                            }
                        )
                    }
                }

                DropdownMenu(expanded = showDirectionMenu, onDismissRequest = { showDirectionMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_asc)) }, onClick = {
                        sortDirection = "asc"; showDirectionMenu = false
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.sort_desc)) }, onClick = {
                        sortDirection = "desc"; showDirectionMenu = false
                    })
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(records) { (id, record) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${stringResource(R.string.title)}: ${record["title"]}", style = MaterialTheme.typography.titleMedium)
                            val artist = record["artist"]?.toString().orEmpty()
                            val year = record["year"]?.toString().orEmpty()
                            val genre = record["genre"]?.toString().orEmpty()
                            val type = record["type"]?.toString().orEmpty()
                            val format = record["format"]?.toString().orEmpty()

                            Text("${stringResource(R.string.artist)}: $artist")
                            if (year.isNotBlank()) {
                                Text("${stringResource(R.string.year)}: $year")
                            }
                            Text("${stringResource(R.string.genre)}: $genre")
                            Text("${stringResource(R.string.type)}: $type")
                            Text("${stringResource(R.string.format)}: $format")
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("edit_record/$id")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                                }

                                IconButton(onClick = {
                                    navController.navigate("details_record/$id")
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.details))
                                }

                                IconButton(onClick = {
                                    recordToDelete = id to record
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    recordToDelete?.let { (recordId, record) ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("records").document(recordId)
                        .delete()
                        .addOnSuccessListener {
                            recordsRaw = recordsRaw.filterNot { it.first == recordId }
                        }
                    recordToDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { Text(record["title"]?.toString()?.let { "\"$it\"" } ?: "") }

        )
    }
}
