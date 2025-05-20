package com.federico.mylibrary.movie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.createTempImageUri
import com.federico.mylibrary.ui.movieFieldModifier
import com.federico.mylibrary.ui.movieFieldTextStyle
import com.federico.mylibrary.uploadCompressedImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.federico.mylibrary.fetchMovieInfoFromTmdb
import com.federico.mylibrary.util.Logger
import com.federico.mylibrary.util.MovieInfoLite
import com.federico.mylibrary.util.TmdbMoviePickerDialog
import com.federico.mylibrary.util.searchMoviesFromTmdb
import com.federico.mylibrary.util.fetchMovieDetailsFromTmdb
import com.federico.mylibrary.util.logCheckpoint
import com.google.firebase.crashlytics.FirebaseCrashlytics


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(navController: NavController, overrideGalleryPicker: (() -> Unit)? = null,
                   overrideCameraPicker: (() -> Unit)? = null,
                   userIdOverride: String? = null) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()


    var showPickerDialog by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<MovieInfoLite>>(emptyList()) }

    var title by remember { mutableStateOf("") }
    var originalTitle by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var cast by remember { mutableStateOf("") }
    var productionCompany by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val formatOptions = listOf(
        stringResource(R.string.format_dvd),
        stringResource(R.string.format_bluray),
        stringResource(R.string.format_digital)
    )

    var expandedFormat by remember { mutableStateOf(false) }
    var uploadingCover by remember { mutableStateOf(false) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val addedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            publishDate = formatter.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    var cameraPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(context, it, userId, folder = "records")
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddMovieScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri.value != null) {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(context, imageUri.value!!, userId,folder = "records")
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddMovieScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(shadowElevation = 4.dp) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val movie = hashMapOf(
                            "title" to title,
                            "originalTitle" to originalTitle,
                            "director" to director,
                            "cast" to cast,
                            "productionCompany" to productionCompany,
                            "genre" to genre,
                            "language" to language,
                            "publishDate" to publishDate,
                            "description" to description,
                            "duration" to (duration.toIntOrNull() ?: 0),
                            "format" to selectedFormat,
                            "addedDate" to addedDate,
                            "rating" to rating.trim(),
                            "notes" to notes,
                            "coverUrl" to coverUrl,
                            "location" to location,
                            "userId" to userId
                        )

                        db.collection("movies").add(movie)
                            .addOnSuccessListener {
                                Toast.makeText(context, context.getString(R.string.movie_added), Toast.LENGTH_SHORT).show()
                                title = ""; originalTitle = ""; director = ""; cast = ""
                                productionCompany = ""; genre = ""; language = ""
                                publishDate = ""; description = ""; duration = ""
                                selectedFormat = ""; rating = ""; notes = ""
                                location = "" ; coverUrl = ""
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "${context.getString(R.string.error_prefix)} ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_movie))
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = movieFieldModifier
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title), fontSize = 14.sp) },
                    textStyle = movieFieldTextStyle,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val results = searchMoviesFromTmdb(title)
                            if (results.isNotEmpty()) {
                                searchResults = results
                                showPickerDialog = true
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_movie_found), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            OutlinedTextField(originalTitle, { originalTitle = it }, label = { Text(stringResource(R.string.original_title), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(director, { director = it }, label = { Text(stringResource(R.string.director), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(cast, { cast = it }, label = { Text(stringResource(R.string.cast), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(productionCompany, { productionCompany = it }, label = { Text(stringResource(R.string.production_company), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(genre, { genre = it }, label = { Text(stringResource(R.string.genre), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(language, { language = it }, label = { Text(stringResource(R.string.book_language), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)


            OutlinedTextField(
                value = publishDate,
                onValueChange = {},
                label = { Text(stringResource(R.string.publish_date), fontSize = 14.sp) },
                textStyle = movieFieldTextStyle,
                modifier = movieFieldModifier,
                readOnly = true,
                enabled = false
            )

            Button(onClick = { showDatePicker = true }) {
                Text(stringResource(R.string.select_date))
            }



            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.book_description), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
            OutlinedTextField(duration, { duration = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.duration_minutes), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)

            ExposedDropdownMenuBox(expanded = expandedFormat, onExpandedChange = { expandedFormat = !expandedFormat }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedFormat,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.format), fontSize = 14.sp) },
                    textStyle = movieFieldTextStyle,
                    modifier = movieFieldModifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedFormat, onDismissRequest = { expandedFormat = false }) {
                    formatOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 14.sp) },
                            onClick = {
                                selectedFormat = option
                                expandedFormat = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(rating, { rating = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.book_rating)) }, modifier = movieFieldModifier)
            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.book_notes)) }, modifier = movieFieldModifier)
            OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.book_location)) }, modifier = movieFieldModifier)
            OutlinedTextField(coverUrl, { coverUrl = it }, label = { Text(stringResource(R.string.book_cover_url)) }, modifier = movieFieldModifier)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    overrideGalleryPicker?.invoke() ?: run {
                        logCheckpoint(context, "📸 bottone galleria premuto")
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        } catch (e: Exception) {
                            //logCheckpoint(context, "❌ errore galleria", e)
                            FirebaseCrashlytics.getInstance().log("crash in AddBookScreen")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            val sw = java.io.StringWriter()
                            Logger.e("GALLERY_ERROR", "Errore lancio galleria", e)
                        }
                    }
                },
                    modifier = Modifier.testTag("galleryButton")) {
                    Text(stringResource(R.string.select_from_gallery))
                }

                Button(onClick = {
                    overrideCameraPicker?.invoke() ?: run {
                        FirebaseCrashlytics.getInstance().log("📸 bottone fotocamera premuto")
                        logCheckpoint(context, "📸 bottone fotocamera premuto")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val permission = Manifest.permission.CAMERA
                            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

                            if (!granted) {
                                // Richiedi il permesso in modo esplicito
                                permissionLauncher.launch(permission)
                                return@Button
                            }
                        }
                        try {
                            val uri = createTempImageUri(context)
                            FirebaseCrashlytics.getInstance().log("📸 URI generato: $uri")
                            /*
                            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                createMediaStoreImageUri(context)
                            } else {
                                createTempImageUri(context)
                            }

                             */
                            imageUri.value = uri
                            Logger.d("DEBUG_URI", "Uri generato: $uri")
                            // PATCH: concedi temporaneamente i permessi di scrittura
                            context.grantUriPermission(
                                "com.android.camera", // oppure "*" per concedere a tutte
                                uri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )


                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().log("crash in addBook con fotocamera")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            logCheckpoint(context, "❌ errore fotocamera", e)
                            val sw = java.io.StringWriter()
                            Logger.e("CAMERA_ERROR", "Errore durante il lancio fotocamera", e)
                        }
                    }
                }, modifier = Modifier.testTag("takePhotoButton")
                ) {
                    Text(stringResource(R.string.take_photo))
                }
            }

            if (uploadingCover) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

                if (showPickerDialog) {
                    TmdbMoviePickerDialog(
                        movies = searchResults,
                        onDismiss = { showPickerDialog = false },
                        onSelect = { selected ->
                            showPickerDialog = false
                            coroutineScope.launch {
                                val details = fetchMovieDetailsFromTmdb(selected.id)
                                if (details != null) {
                                    title = details.title
                                    originalTitle = details.originalTitle
                                    description = details.description
                                    publishDate = details.publishDate
                                    genre = details.genre
                                    duration = if (details.duration > 0) details.duration.toString() else ""
                                    productionCompany = details.productionCompany
                                    director = details.director
                                    cast = details.cast
                                    language = details.language
                                    coverUrl = details.coverUrl
                                    Toast.makeText(context, context.getString(R.string.movie_data_loaded), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

            }
    }
}
