
package com.federico.mylibrary.record

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.uploadCompressedImage
import com.federico.mylibrary.createTempImageUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddRecordScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("Digital") }
    var year by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var physicalSupport by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(context.getString(R.string.track)) }

    var trackNumber by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var soloists by remember { mutableStateOf("") }

    var tracklistText by remember { mutableStateOf("") }
    var totalTracks by remember { mutableStateOf("") }
    var multiAlbum by remember { mutableStateOf(false) }

    var language by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var addedDate = currentDate
    var location by remember { mutableStateOf("") }

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploading = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(context, it, userId, folder = "records")
                    coverUrl = downloadUrl
                    Toast.makeText(
                        context,
                        context.getString(R.string.cover_uploaded),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddRecordScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(
                        context,
                        context.getString(R.string.upload_failed, e.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    uploading = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri.value != null) {
            uploading = true
            coroutineScope.launch {
                try {
                    val downloadUrl =
                        uploadCompressedImage(context, imageUri.value!!, userId, folder = "records")
                    coverUrl = downloadUrl
                    Toast.makeText(
                        context,
                        context.getString(R.string.cover_uploaded),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddRecordScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(
                        context,
                        context.getString(R.string.upload_failed, e.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    uploading = false
                }
            }
        }
    }

    fun saveRecord() {
        val tracklist = tracklistText.lines().filter { it.isNotBlank() }

        val record = hashMapOf(
            "title" to title,
            "artist" to artist,
            "format" to format,
            "year" to year,
            "genre" to genre,
            "physicalSupport" to physicalSupport,
            "type" to type,
            "trackNumber" to trackNumber.toIntOrNull(),
            "album" to album,
            "duration" to duration,
            "label" to label,
            "soloists" to soloists,
            "tracklist" to tracklist,
            "totalTracks" to totalTracks.toIntOrNull(),
            "multiAlbum" to multiAlbum,
            "language" to language,
            "description" to description,
            "rating" to rating,
            "notes" to notes,
            "coverUrl" to coverUrl,
            "addedDate" to addedDate,
            "location" to location,
            "userId" to userId
        )

        db.collection("records")
            .add(record)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.record_added),
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_prefix) + " ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    Scaffold(
        topBar = {
            Button(
                onClick = { saveRecord() },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(stringResource(R.string.save_record))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                title,
                { title = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                artist,
                { artist = it },
                label = { Text(stringResource(R.string.artist)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                year,
                { year = it },
                label = { Text(stringResource(R.string.year)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                genre,
                { genre = it },
                label = { Text(stringResource(R.string.genre)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                Text(stringResource(R.string.type))
                Spacer(Modifier.width(8.dp))
                listOf(stringResource(R.string.track), stringResource(R.string.album)).forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == it, onClick = { type = it })
                        Text(it)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            OutlinedTextField(
                format,
                { format = it },
                label = { Text(stringResource(R.string.format)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = physicalSupport, onCheckedChange = { physicalSupport = it })
                Text(stringResource(R.string.physical_support))
            }

            if (type == stringResource(R.string.track)) {
                OutlinedTextField(
                    album,
                    { album = it },
                    label = { Text(stringResource(R.string.album)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    trackNumber,
                    { trackNumber = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.track_number)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    duration,
                    { duration = it },
                    label = { Text(stringResource(R.string.duration)) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    totalTracks,
                    { totalTracks = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.total_tracks)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    tracklistText,
                    { tracklistText = it },
                    label = { Text(stringResource(R.string.tracklist)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = multiAlbum, onCheckedChange = { multiAlbum = it })
                    Text(stringResource(R.string.multi_album))
                }
            }

            OutlinedTextField(
                label,
                { label = it },
                label = { Text(stringResource(R.string.label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                soloists,
                { soloists = it },
                label = { Text(stringResource(R.string.soloists)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                language,
                { language = it },
                label = { Text(stringResource(R.string.language)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                description,
                { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                rating,
                { rating = it },
                label = { Text(stringResource(R.string.rating)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                notes,
                { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                location,
                { location = it },
                label = { Text(stringResource(R.string.location)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (type == stringResource(R.string.album)) {
                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text(stringResource(R.string.cover_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        imageUri.value = createTempImageUri(context)
                        cameraLauncher.launch(imageUri.value)
                    }) {
                        Text(stringResource(R.string.take_cover_photo))
                    }
                    Button(onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text(stringResource(R.string.select_cover_photo))
                    }
                }

                if (uploading) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }
            }

        }
    }
}
