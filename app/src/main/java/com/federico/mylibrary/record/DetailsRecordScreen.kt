
package com.federico.mylibrary.record

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            val coverUrl = record?.get("coverUrl")?.toString()
            if (!coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(coverUrl).crossfade(true).build(),
                    contentDescription = stringResource(R.string.cover_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text("${stringResource(R.string.title)}: ${record?.get("title")}", style = MaterialTheme.typography.headlineSmall)
            Text("${stringResource(R.string.artist)}: ${record?.get("artist")}")
            Text("${stringResource(R.string.genre)}: ${record?.get("genre")}")
            Text("${stringResource(R.string.year)}: ${record?.get("year")}")
            Text("${stringResource(R.string.type)}: ${record?.get("type")}")
            Text("${stringResource(R.string.format)}: ${record?.get("format")}")
            Text("${stringResource(R.string.album)}: ${record?.get("album")}")
            Text("${stringResource(R.string.track_number)}: ${record?.get("trackNumber")}")
            Text("${stringResource(R.string.duration)}: ${record?.get("duration")}")
            Text("${stringResource(R.string.label)}: ${record?.get("label")}")
            Text("${stringResource(R.string.soloists)}: ${record?.get("soloists")}")
            Text("${stringResource(R.string.total_tracks)}: ${record?.get("totalTracks")}")
            Text("${stringResource(R.string.multi_album)}: ${(record?.get("multiAlbum") as? Boolean)?.toString()}")
            Text("${stringResource(R.string.physical_support)}: ${(record?.get("physicalSupport") as? Boolean)?.toString()}")
            Text("${stringResource(R.string.language)}: ${record?.get("language")}")
            Text("${stringResource(R.string.rating)}: ${record?.get("rating")}")
            Text("${stringResource(R.string.notes)}: ${record?.get("notes")}")
            Text("${stringResource(R.string.location)}: ${record?.get("location")}")
            Text("${stringResource(R.string.added_date)}: ${record?.get("addedDate")}")
            Text("${stringResource(R.string.cover_url)}: ${record?.get("coverUrl")}")
            val tracklist = (record?.get("tracklist") as? List<*>)?.filterIsInstance<String>()?.joinToString("\n") ?: ""
            if (tracklist.isNotBlank()) {
                Text("${stringResource(R.string.tracklist)}:\n$tracklist")
            }

            val description = record?.get("description")?.toString() ?: ""
            if (description.isNotBlank()) {
                Text("${stringResource(R.string.description)}:\n$description")
            }

        }
    }
}
