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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.createTempImageUri
import com.federico.mylibrary.model.Game
import com.federico.mylibrary.uploadCompressedImage
import com.federico.mylibrary.ui.bookFieldModifier
import com.federico.mylibrary.ui.bookFieldTextStyle
import com.federico.mylibrary.util.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGameScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val gameId = backStackEntry.arguments?.getString("gameId") ?: return
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val typeOptions = listOf(
        stringResource(R.string.game_type_board),
        stringResource(R.string.game_type_videogame),
        stringResource(R.string.game_type_other)
    )
    val typeKeys = listOf("board", "videogame", "altro")


    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(typeOptions[0]) }
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
    var addedDate by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var uploadingCover by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var expandedType by remember { mutableStateOf(false) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploadingCover = true
            coroutineScope.launch {
                try {
                    coverUrl = uploadCompressedImage(context, it, userId, folder = "games")
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in EditGameScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
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
                    coverUrl = uploadCompressedImage(context, imageUri.value!!, userId, folder = "games")
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in EditGameScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    uploadingCover = false
                }
            }
        }
    }

    LaunchedEffect(gameId) {
        val doc = db.collection("games").document(gameId).get().await()
        val game = doc.toObject(Game::class.java)
        game?.let {
            title = it.title
            type = it.type
            platform = it.platform
            publisher = it.publisher
            releaseDate = it.releaseDate
            genre = it.genre
            language = it.language
            description = it.description
            minPlayers = it.minPlayers.takeIf { p -> p > 0 }?.toString() ?: ""
            maxPlayers = it.maxPlayers.takeIf { p -> p > 0 }?.toString() ?: ""
            durationMinutes = it.durationMinutes.takeIf { d -> d > 0 }?.toString() ?: ""
            rating = it.rating
            notes = it.notes
            location = it.location
            addedDate = it.addedDate
            coverUrl = it.coverUrl
        }
        isLoading = false
    }

    val missingTitle = stringResource(R.string.missing_title)
    val gameUpdated = stringResource(R.string.game_updated)
    val errorPrefix = stringResource(R.string.error_prefix)

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val updateMap = mapOf(
                            "title" to title,
                            "type" to type,
                            "platform" to platform,
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
                            "addedDate" to addedDate,
                            "coverUrl" to coverUrl
                        )

                        db.collection("games").document(gameId)
                            .update(updateMap)
                            .addOnSuccessListener {
                                Toast.makeText(context, gameUpdated, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "$errorPrefix ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(stringResource(R.string.save_game))
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                @Composable
                fun inputField(value: String, onChange: (String) -> Unit, label: Int) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        label = { Text(stringResource(label), fontSize = 14.sp) },
                        textStyle = bookFieldTextStyle,
                        modifier = bookFieldModifier
                    )
                }

                inputField(title, { title = it }, R.string.game_title)

                val selectedIndex = typeKeys.indexOf(type).coerceAtLeast(0)

                ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = typeOptions[selectedIndex],
                        onValueChange = {},
                        label = { Text(stringResource(R.string.game_type)) },
                        modifier = Modifier.fillMaxWidth()
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
                    inputField(platform, { platform = it }, R.string.game_platform)
                }

                inputField(publisher, { publisher = it }, R.string.game_publisher)
                inputField(releaseDate, { releaseDate = it }, R.string.game_release_date)
                inputField(genre, { genre = it }, R.string.game_genre)
                inputField(language, { language = it }, R.string.game_language)
                inputField(description, { description = it }, R.string.game_description)
                inputField(minPlayers, { minPlayers = it.filter { c -> c.isDigit() } }, R.string.game_min_players)
                inputField(maxPlayers, { maxPlayers = it.filter { c -> c.isDigit() } }, R.string.game_max_players)
                inputField(durationMinutes, { durationMinutes = it.filter { c -> c.isDigit() } }, R.string.game_duration_minutes)
                inputField(rating, { rating = it.filter { c -> c.isDigit() } }, R.string.game_rating)
                inputField(notes, { notes = it }, R.string.game_notes)
                inputField(location, { location = it }, R.string.game_location)
                inputField(coverUrl, { coverUrl = it }, R.string.game_cover_url)
                inputField(addedDate, {}, R.string.game_added_date)

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
                            Logger.e("GALLERY_ERROR", "EditBook Errore lancio galleria", e)
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
                            FirebaseCrashlytics.getInstance().log("EditRecord crash in addBook con fotocamera")
                            val sw = java.io.StringWriter()
                            Logger.e("CAMERA_ERROR", "EditRecord Errore durante il lancio fotocamera", e)
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
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
