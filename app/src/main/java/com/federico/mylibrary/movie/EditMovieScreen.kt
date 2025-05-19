package com.federico.mylibrary.movie

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.createTempImageUri
import com.federico.mylibrary.model.Movie
import com.federico.mylibrary.ui.movieFieldModifier
import com.federico.mylibrary.ui.movieFieldTextStyle
import com.federico.mylibrary.uploadCompressedImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMovieScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val movieId = backStackEntry.arguments?.getString("movieId") ?: return
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

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
    var format by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    val imageUri = remember { mutableStateOf<android.net.Uri?>(null) }
    var uploadingCover by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(context, it, FirebaseAuth.getInstance().currentUser?.uid ?: "", folder = "movies")
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in EditMovieScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri.value != null) {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(context, imageUri.value!!, FirebaseAuth.getInstance().currentUser?.uid ?: "", folder = "movies")
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in EditMovieScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

    LaunchedEffect(movieId) {
        val doc = db.collection("movies").document(movieId).get().await()
        val movie = doc.toObject(Movie::class.java)
        movie?.let {
            title = it.title
            originalTitle = it.originalTitle
            director = it.director
            cast = it.cast
            productionCompany = it.productionCompany
            genre = it.genre
            language = it.language
            publishDate = it.publishDate
            description = it.description
            duration = it.duration.toString()
            format = it.format
            rating = it.rating
            notes = it.notes
            coverUrl = it.coverUrl
            location = it.location
        }
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val updateMap = mapOf(
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
                                "format" to format,
                                "rating" to rating.trim(),
                                "notes" to notes,
                                "coverUrl" to coverUrl,
                                "location" to location
                            )

                            db.collection("movies").document(movieId)
                                .update(updateMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, context.getString(R.string.movie_updated), Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
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
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.title), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(originalTitle, { originalTitle = it }, label = { Text(stringResource(R.string.original_title), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(director, { director = it }, label = { Text(stringResource(R.string.director), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(cast, { cast = it }, label = { Text(stringResource(R.string.cast), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(productionCompany, { productionCompany = it }, label = { Text(stringResource(R.string.production_company), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(genre, { genre = it }, label = { Text(stringResource(R.string.genre), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(language, { language = it }, label = { Text(stringResource(R.string.book_language), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(publishDate, { publishDate = it }, label = { Text(stringResource(R.string.publish_date), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.book_description), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(duration, { duration = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.duration_minutes), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(format, { format = it }, label = { Text(stringResource(R.string.format), fontSize = 14.sp) }, textStyle = movieFieldTextStyle, modifier = movieFieldModifier)
                OutlinedTextField(rating, { rating = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.book_rating)) }, modifier = movieFieldModifier)
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.book_notes)) }, modifier = movieFieldModifier)
                OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.book_location)) }, modifier = movieFieldModifier)
                OutlinedTextField(coverUrl, { coverUrl = it }, label = { Text(stringResource(R.string.book_cover_url)) }, modifier = movieFieldModifier)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text(stringResource(R.string.select_from_gallery))
                    }

                    Button(onClick = {
                        imageUri.value = createTempImageUri(context)
                        imageUri.value?.let { cameraLauncher.launch(it) }
                    }) {
                        Text(stringResource(R.string.take_photo))
                    }
                }

                if (uploadingCover) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
