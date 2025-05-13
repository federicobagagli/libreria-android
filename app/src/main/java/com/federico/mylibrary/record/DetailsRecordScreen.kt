
package com.federico.mylibrary.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.federico.mylibrary.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun DetailsRecordScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val recordId = backStackEntry.arguments?.getString("recordId") ?: return
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var record by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(recordId) {
        val doc = db.collection("records").document(recordId).get().await()
        record = doc.data
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier.padding(16.dp)) {
            CircularProgressIndicator()
        }
    } else if (record == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.no_data_found))
        }
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val coverUrl = record?.get("coverUrl")?.toString().orEmpty()
            if (coverUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(coverUrl).crossfade(true).build(),
                    contentDescription = stringResource(R.string.cover_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }
            val yes = stringResource(R.string.yes)
            val no = stringResource(R.string.no)
            fun getString(key: String) = record?.get(key)?.toString().orEmpty()
            fun getLongString(key: String) = (record?.get(key) as? Long)?.toString().orEmpty()
            fun getBooleanLabel(key: String) = when (record?.get(key) as? Boolean) {
                true -> yes
                false -> no
                null -> "-"
            }

            Text("${stringResource(R.string.title)}: ${getString("title")}", style = MaterialTheme.typography.headlineSmall)
            Text("${stringResource(R.string.artist)}: ${getString("artist")}")
            Text("${stringResource(R.string.genre)}: ${getString("genre")}")
            Text("${stringResource(R.string.year)}: ${getString("year")}")
            Text("${stringResource(R.string.type)}: ${getString("type")}")
            Text("${stringResource(R.string.format)}: ${getString("format")}")
            Text("${stringResource(R.string.album)}: ${getString("album")}")
            Text("${stringResource(R.string.track_number)}: ${getLongString("trackNumber")}")
            Text("${stringResource(R.string.duration)}: ${getString("duration")}")
            Text("${stringResource(R.string.label)}: ${getString("label")}")
            Text("${stringResource(R.string.soloists)}: ${getString("soloists")}")
            Text("${stringResource(R.string.total_tracks)}: ${getLongString("totalTracks")}")
            Text("${stringResource(R.string.multi_album)}: ${getBooleanLabel("multiAlbum")}")
            Text("${stringResource(R.string.physical_support)}: ${getBooleanLabel("physicalSupport")}")
            Text("${stringResource(R.string.language)}: ${getString("language")}")
            Text("${stringResource(R.string.rating)}: ${getString("rating")}")
            Text("${stringResource(R.string.notes)}: ${getString("notes")}")
            Text("${stringResource(R.string.location)}: ${getString("location")}")
            Text("${stringResource(R.string.added_date)}: ${getString("addedDate")}")
            Text("${stringResource(R.string.cover_url)}: $coverUrl")

            val tracklist = (record?.get("tracklist") as? List<*>)?.filterIsInstance<String>()?.joinToString("\n") ?: ""
            if (tracklist.isNotBlank()) {
                Text("${stringResource(R.string.tracklist)}:\n$tracklist")
            }

            val description = getString("description")
            if (description.isNotBlank()) {
                Text("${stringResource(R.string.description)}:\n$description")
            }
        }
    }
}
