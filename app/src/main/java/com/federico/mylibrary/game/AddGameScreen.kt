package com.federico.mylibrary.game

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.federico.mylibrary.R
import com.federico.mylibrary.createTempImageUri
import com.federico.mylibrary.model.Game
import com.federico.mylibrary.ui.LimitReachedDialog
import com.federico.mylibrary.uploadCompressedImage
import com.federico.mylibrary.ui.bookFieldModifier
import com.federico.mylibrary.ui.bookFieldTextStyle
import com.federico.mylibrary.ui.movieFieldModifier
import com.federico.mylibrary.ui.movieFieldTextStyle
import com.federico.mylibrary.util.Logger
import com.federico.mylibrary.util.checkLimitReached
import com.federico.mylibrary.util.logCheckpoint
import com.federico.mylibrary.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(navController: NavHostController,
                  userViewModel: UserViewModel,
                  overrideGalleryPicker: (() -> Unit)? = null,
                  overrideCameraPicker: (() -> Unit)? = null,
                  userIdOverride: String? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("board") }
    var platform by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minPlayers by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var uploadingCover by remember { mutableStateOf(false) }

    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    val typeOptions = listOf(
        stringResource(R.string.game_type_board),
        stringResource(R.string.game_type_videogame),
        stringResource(R.string.game_type_other)
    )
    val typeKeys = listOf("board", "videogame", "altro")

    val isPremium by userViewModel.isPremium.collectAsState()
    var isLimitReached by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    //per limitare numero inserimenti per i non-premium
    LaunchedEffect(Unit) {
        if (!isPremium) {
            checkLimitReached(userId, "games", userViewModel.maxItemsNonPremium) { reached ->
                isLimitReached = reached
            }
        }
    }

    var expandedType by remember { mutableStateOf(false) }

    val imageUri = remember { mutableStateOf<Uri?>(null) }

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
        if (uri == null) return@rememberLauncherForActivityResult
        uploadingCover = true
        coroutineScope.launch {
            try {
                val downloadUrl = uploadCompressedImage(context, uri, userId, folder = "games")
                coverUrl = downloadUrl
                Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("crash in AddGameScreen")
                FirebaseCrashlytics.getInstance().recordException(e)
                Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
            } finally {
                uploadingCover = false
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
                    val downloadUrl = uploadCompressedImage(context, imageUri.value!!, userId, folder = "games")
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddGameScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

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
                            releaseDate = formatter.format(Date(millis))
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

    fun saveGame(game: Map<String, Any>) {
        db.collection("games")
            .add(game)
            .addOnSuccessListener {
                Toast.makeText(context, context.getString(R.string.game_added), Toast.LENGTH_SHORT).show()
                title = ""
                type = "board"
                platform = ""
                publisher = ""
                releaseDate = ""
                genre = ""
                language = ""
                description = ""
                minPlayers = ""
                maxPlayers = ""
                durationMinutes = ""
                rating = ""
                notes = ""
                location = ""
                coverUrl = ""
            }
            .addOnFailureListener {
                Toast.makeText(context, context.getString(R.string.error_prefix) + " ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isLimitReached) {
                            showLimitDialog = true
                            return@Button
                        }
                        if (title.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.missing_title), Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val game = mutableMapOf(
                            "title" to title,
                            "type" to type,
                            "publisher" to publisher,
                            "releaseDate" to releaseDate,
                            "genre" to genre,
                            "language" to language,
                            "description" to description,
                            "minPlayers" to (minPlayers.toIntOrNull() ?: 0),
                            "maxPlayers" to (maxPlayers.toIntOrNull() ?: 0),
                            "durationMinutes" to (durationMinutes.toIntOrNull() ?: 0),
                            "rating" to rating,
                            "notes" to notes,
                            "location" to location,
                            "addedDate" to currentDate,
                            "userId" to userId,
                            "coverUrl" to coverUrl
                        )
                        if (type == "videogame") game["platform"] = platform
                        saveGame(game)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_game))
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.game_title), fontSize = 14.sp) }, textStyle = bookFieldTextStyle, modifier = bookFieldModifier)

            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                OutlinedTextField(
                    readOnly = true,
                    value = stringResource(id = when (type) {
                        "board" -> R.string.game_type_board
                        "videogame" -> R.string.game_type_videogame
                        else -> R.string.game_type_other
                    }),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.game_type), fontSize = 14.sp) },
                    modifier = bookFieldModifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    typeOptions.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                type = typeKeys[index]
                                expandedType = false
                            }
                        )
                    }
                }
            }

            if (type == "videogame") {
                OutlinedTextField(platform, { platform = it }, label = { Text(stringResource(R.string.game_platform)) }, modifier = bookFieldModifier)
            }

            OutlinedTextField(publisher, { publisher = it }, label = { Text(stringResource(R.string.game_publisher)) }, modifier = bookFieldModifier)

            OutlinedTextField(
                value = releaseDate,
                onValueChange = {},
                label = { Text(stringResource(R.string.game_release_date), fontSize = 14.sp) },
                textStyle = movieFieldTextStyle,
                modifier = movieFieldModifier,
                readOnly = true,
                enabled = false
            )

            Button(onClick = { showDatePicker = true }) {
                Text(stringResource(R.string.select_date))
            }

            OutlinedTextField(genre, { genre = it }, label = { Text(stringResource(R.string.game_genre)) }, modifier = bookFieldModifier)
            OutlinedTextField(language, { language = it }, label = { Text(stringResource(R.string.game_language)) }, modifier = bookFieldModifier)
            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.game_description)) }, modifier = bookFieldModifier)
            OutlinedTextField(minPlayers, { minPlayers = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.game_min_players)) }, modifier = bookFieldModifier)
            OutlinedTextField(maxPlayers, { maxPlayers = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.game_max_players)) }, modifier = bookFieldModifier)
            OutlinedTextField(durationMinutes, { durationMinutes = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.game_duration_minutes)) }, modifier = bookFieldModifier)
            OutlinedTextField(rating, { rating = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.game_rating)) }, modifier = bookFieldModifier)
            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.game_notes)) }, modifier = bookFieldModifier)
            OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.game_location)) }, modifier = bookFieldModifier)
            OutlinedTextField(coverUrl, { coverUrl = it }, label = { Text(stringResource(R.string.game_cover_url)) }, modifier = bookFieldModifier)

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

        }
    }
    LimitReachedDialog(
        show = showLimitDialog,
        onDismiss = { showLimitDialog = false },
        onGoPremium = {
            showLimitDialog = false
            navController.navigate("go_premium")
        }
    )
}
