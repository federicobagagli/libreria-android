package com.federico.mylibrary.book

import android.content.Intent
import android.widget.Toast
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
import com.federico.mylibrary.model.Book
import com.federico.mylibrary.ui.bookFieldModifier
import com.federico.mylibrary.ui.bookFieldTextStyle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.federico.mylibrary.uploadCompressedImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.remember
import com.federico.mylibrary.createTempImageUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val bookId = backStackEntry.arguments?.getString("bookId") ?: return
    val db = FirebaseFirestore.getInstance()

    val formatOptions = listOf(
        stringResource(R.string.format_physical),
        stringResource(R.string.format_ebook),
        stringResource(R.string.format_audio)
    )

    val readingOptions = listOf(
        stringResource(R.string.status_not_started),
        stringResource(R.string.status_reading),
        stringResource(R.string.status_completed)
    )

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(formatOptions[0]) }
    var selectedReadingStatus by remember { mutableStateOf(readingOptions[0]) }
    var addedDate by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var expandedFormat by remember { mutableStateOf(false) }
    var expandedReading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var uploadingCover by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val imageUri = remember { mutableStateOf<Uri?>(null) }




    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    val downloadUrl = uploadCompressedImage(
                        context = context,
                        imageUri = it,
                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch,
                        folder = "covers"
                    )
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
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
                    val downloadUrl = uploadCompressedImage(
                        context = context,
                        imageUri = imageUri.value!!,
                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch,
                        folder = "covers"
                    )
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

    LaunchedEffect(bookId) {
        val doc = db.collection("books").document(bookId).get().await()
        val book = doc.toObject(Book::class.java)
        if (book != null) {
            title = book.title
            author = book.author
            genre = book.genre
            publishDate = book.publishDate
            publisher = book.publisher
            language = book.language
            description = book.description
            pageCount = if (book.pageCount > 0) book.pageCount.toString() else ""
            selectedFormat = book.format
            selectedReadingStatus = book.readingStatus
            addedDate = book.addedDate
            rating = book.rating.takeIf { it.toIntOrNull() != null && it.toInt() > 0 } ?: ""
            notes = book.notes
            coverUrl = book.coverUrl
            location = book.location
        }
        isLoading = false
    }

    val scrollState = rememberScrollState()
    val missingTitle = stringResource(R.string.missing_title)
    val bookUpdated = stringResource(R.string.book_updated)
    val errorPrefix = stringResource(R.string.error_prefix)

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // 🔼 Pulsante "Salva libro" in alto, fisso
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val updateMap = mutableMapOf<String, Any>(
                                "title" to title,
                                "author" to author,
                                "publisher" to publisher,
                                "genre" to genre,
                                "language" to language,
                                "publishDate" to publishDate,
                                "description" to description,
                                "pageCount" to (pageCount.toIntOrNull() ?: 0),
                                "format" to selectedFormat,
                                "readingStatus" to selectedReadingStatus,
                                "addedDate" to addedDate,
                                "rating" to rating.trim(),
                                "notes" to notes,
                                "coverUrl" to coverUrl,
                                "location" to location
                            )

                            val currentDate = java.text.SimpleDateFormat(
                                "yyyy-MM-dd",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date())
                            if (selectedReadingStatus.lowercase() == context.getString(R.string.status_completed)
                                    .lowercase()
                            ) {
                                updateMap["readDate"] = currentDate
                            }

                            db.collection("books").document(bookId)
                                .update(updateMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, bookUpdated, Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "$errorPrefix ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_book))
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(stringResource(R.string.author), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text(stringResource(R.string.book_publisher), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text(stringResource(R.string.genre), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text(stringResource(R.string.book_language), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = publishDate,
                    onValueChange = { publishDate = it },
                    label = { Text(stringResource(R.string.publish_date), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.book_description), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = pageCount,
                    onValueChange = { pageCount = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.book_page_count), fontSize = 14.sp) },
                    textStyle = bookFieldTextStyle,
                    modifier = bookFieldModifier
                )

                ExposedDropdownMenuBox(
                    expanded = expandedFormat,
                    onExpandedChange = { expandedFormat = !expandedFormat }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedFormat,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.format), fontSize = 14.sp) },
                        textStyle = bookFieldTextStyle,
                        modifier = bookFieldModifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFormat,
                        onDismissRequest = { expandedFormat = false }) {
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

                ExposedDropdownMenuBox(
                    expanded = expandedReading,
                    onExpandedChange = { expandedReading = !expandedReading }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedReadingStatus,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.reading_status), fontSize = 14.sp) },
                        textStyle = bookFieldTextStyle,
                        modifier = bookFieldModifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedReading,
                        onDismissRequest = { expandedReading = false }) {
                        readingOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 14.sp) },
                                onClick = {
                                    selectedReadingStatus = option
                                    expandedReading = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.book_rating)) },
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.book_notes)) },
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = addedDate,
                    onValueChange = {},
                    enabled = false,
                    label = { Text(stringResource(R.string.book_added_date)) },
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.book_location)) },
                    modifier = bookFieldModifier
                )
                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text(stringResource(R.string.book_cover_url)) },
                    modifier = bookFieldModifier
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                            val sw = java.io.StringWriter()
                            e.printStackTrace(java.io.PrintWriter(sw))
                            Log.e("GALLERY_ERROR", "EditBook Errore lancio galleria", e)
                        }
                    }) {
                        Text(stringResource(R.string.select_from_gallery))
                    }

                    Button(onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val permission = Manifest.permission.CAMERA
                                val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

                                if (!granted) {
                                    permissionLauncher.launch(permission)
                                    return@Button
                                }
                            }

                            val uri = createTempImageUri(context)
                            imageUri.value = uri

                            context.grantUriPermission(
                                "com.android.camera",
                                uri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                            FirebaseCrashlytics.getInstance().log("EditBook crash in addBook con fotocamera")
                            val sw = java.io.StringWriter()
                            e.printStackTrace(java.io.PrintWriter(sw))
                            Log.e("CAMERA_ERROR", "EditBook Errore durante il lancio fotocamera", e)
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }) {
                        Text(stringResource(R.string.take_photo))
                    }
                }


                if (uploadingCover) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }



                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val currentDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        val updateMap = mutableMapOf<String, Any>(
                            "title" to title,
                            "author" to author,
                            "publisher" to publisher,
                            "genre" to genre,
                            "language" to language,
                            "publishDate" to publishDate,
                            "description" to description,
                            "pageCount" to (pageCount.toIntOrNull() ?: 0),
                            "format" to selectedFormat,
                            "readingStatus" to selectedReadingStatus,
                            "addedDate" to addedDate,
                            "rating" to rating.trim(),
                            "notes" to notes,
                            "coverUrl" to coverUrl,
                            "location" to location
                        )

                        // Aggiungi readDate solo se si passa a "completato"
                        if (selectedReadingStatus.lowercase() == context.getString(R.string.status_completed)
                                .lowercase()
                        ) {
                            updateMap["readDate"] = currentDate
                        }

                        db.collection("books").document(bookId)
                            .update(updateMap)
                            .addOnSuccessListener {
                                Toast.makeText(context, bookUpdated, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "$errorPrefix ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_book))
                }
            }
        }
    }
}
