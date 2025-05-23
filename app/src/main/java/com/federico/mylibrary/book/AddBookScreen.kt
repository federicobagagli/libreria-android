package com.federico.mylibrary.book

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.federico.mylibrary.BuildConfig
import com.federico.mylibrary.ui.bookFieldModifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import com.federico.mylibrary.createTempImageUri
import com.federico.mylibrary.ui.bookFieldTextStyle
import com.google.mlkit.vision.common.InputImage
import com.federico.mylibrary.uploadCompressedImage
import androidx.core.content.ContextCompat
import com.federico.mylibrary.util.logCheckpoint
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import com.federico.mylibrary.ui.LimitReachedDialog
import com.federico.mylibrary.ui.movieFieldModifier
import com.federico.mylibrary.ui.movieFieldTextStyle
import com.federico.mylibrary.util.Logger
import com.federico.mylibrary.viewmodel.UserViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.federico.mylibrary.util.checkLimitReached
import com.federico.mylibrary.util.stringResourceByName

@Serializable
data class BookInfo(
    val title: String = "",
    val authors: List<AuthorInfo> = emptyList(),
    val genre: String = "",
    val publishDate: String = "",
    val publisher: String = "",
    val language: String = "",
    val description: String = "",
    val pageCount: Int = 0,
    val averageRating: Double = 0.0,
    val coverUrl: String = "",
    val location: String = ""
)

@Serializable
data class AuthorInfo(val name: String)

suspend fun fetchBookInfoFromGoogleBooks(isbn: String, apiKey: String): BookInfo? {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    return try {
        val response: JsonObject = client.get("https://www.googleapis.com/books/v1/volumes") {
            url {
                parameters.append("q", "isbn:$isbn")
                parameters.append("key", apiKey)
            }
        }.body()
        Logger.d("GoogleBooksAPI", "Response JSON: $response")
        val items = response["items"]?.jsonArray ?: return null
        val volumeInfo = items[0].jsonObject["volumeInfo"]?.jsonObject ?: return null

        val title = volumeInfo["title"]?.jsonPrimitive?.content ?: ""
        val authorsArray = volumeInfo["authors"]?.jsonArray?.mapNotNull {
            it.jsonPrimitive.contentOrNull
        } ?: emptyList()
        val genre = volumeInfo["categories"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
        val publishDate = volumeInfo["publishedDate"]?.jsonPrimitive?.content ?: ""
        val publisher = volumeInfo["publisher"]?.jsonPrimitive?.content ?: ""
        val language = volumeInfo["language"]?.jsonPrimitive?.content ?: ""
        val description = volumeInfo["description"]?.jsonPrimitive?.content ?: ""
        val pageCount = volumeInfo["pageCount"]?.jsonPrimitive?.intOrNull ?: 0
        val averageRating = volumeInfo["averageRating"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val coverUrl =
            volumeInfo["imageLinks"]?.jsonObject?.get("thumbnail")?.jsonPrimitive?.content ?: ""
        val location = volumeInfo["location"]?.jsonPrimitive?.content ?: ""

        BookInfo(
            title = title,
            authors = authorsArray.map { AuthorInfo(it) },
            genre = genre,
            publishDate = publishDate,
            publisher = publisher,
            language = language,
            description = description,
            pageCount = pageCount,
            averageRating = averageRating,
            coverUrl = coverUrl,
            location = location
        )
    } catch (e: Exception) {
        null
    } finally {
        client.close()
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(navController: NavHostController, userViewModel: UserViewModel, overrideGalleryPicker: (() -> Unit)? = null,
                  overrideCameraPicker: (() -> Unit)? = null,
                  userIdOverride: String? = null) {

    val context = LocalContext.current

    val scannedIsbn = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("scannedIsbn")

    //test debug
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var errorStackTrace by remember { mutableStateOf("") }
    //fine test debug

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    var uploadingCover by remember { mutableStateOf(false) }
    var showIsbnHelpDialog by remember { mutableStateOf(false) }

    val readingStatusKeys = listOf("not_started", "reading", "completed")
    val formatKeys = listOf("physical", "ebook", "audio")

    val readingStatusOptions = readingStatusKeys.map { it to stringResourceByName("status_$it") }
    val formatOptions = formatKeys.map { it to stringResourceByName("format_$it") }

    var selectedReadingKey by remember { mutableStateOf("not_started") }
    var selectedFormatKey by remember { mutableStateOf("physical") }

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var pendingBookData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()


    val userId = userIdOverride ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
    //val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(formatOptions[0]) }
    var selectedReadingStatus by remember { mutableStateOf("") }
    var addedDate by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var expandedFormat by remember { mutableStateOf(false) }
    var expandedReading by remember { mutableStateOf(false) }

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    addedDate = currentDate

    val saveBookLabel = stringResource(R.string.save_book)
    val bookAdded = stringResource(R.string.book_added)
    val errorPrefix = stringResource(R.string.error_prefix)
    val missingTitle = stringResource(R.string.missing_title)

    val isPremium by userViewModel.isPremium.collectAsState()
    var isLimitReached by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    //per limitare numero inserimenti per i non-premium
    LaunchedEffect(Unit) {
        if (!isPremium) {
            checkLimitReached(userId, "books", userViewModel.maxItemsNonPremium) { reached ->
                isLimitReached = reached
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

    val barcodeScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        if (bitmap != null) {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val image = InputImage.fromBitmap(bitmap, 0)

            Toast.makeText(context, context.getString(R.string.processing_image), Toast.LENGTH_SHORT).show()
            Logger.d("ScanISBN", "Elaborazione immagine per barcode...")

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    Logger.d("ScanISBN", "Barcode trovati: ${barcodes.size}")

                    val isbnBarcode = barcodes.firstOrNull {
                        Toast.makeText(context, "Rilevato: ${it.rawValue}", Toast.LENGTH_SHORT)
                            .show()
                        it.rawValue?.length == 13 && it.format == Barcode.FORMAT_EAN_13
                    }

                    val isbn = isbnBarcode?.rawValue

                    if (isbn != null) {
                        Toast.makeText(context, context.getString(R.string.isbn_found, isbn), Toast.LENGTH_SHORT).show()
                        Logger.d("ScanISBN", "ISBN valido: $isbn")

                        coroutineScope.launch {
                            val book =
                                fetchBookInfoFromGoogleBooks(isbn, BuildConfig.GOOGLE_BOOKS_API_KEY)
                            if (book != null) {
                                title = book.title
                                author = book.authors.joinToString(", ") { it.name }
                                genre = book.genre
                                publishDate = book.publishDate
                                publisher = book.publisher
                                language = book.language
                                description = book.description
                                pageCount =
                                    if (book.pageCount > 0) book.pageCount.toString() else ""
                                rating = if (book.averageRating > 0.0) book.averageRating.toInt()
                                    .toString() else ""
                                coverUrl = book.coverUrl
                                location = book.location

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.text_recognized),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Logger.d(
                                    "ScanISBN",
                                    "Libro trovato: ${book.title} - ${book.authors.joinToString(", ") { it.name }}"
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.no_book_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Logger.d("ScanISBN", "Nessun libro trovato via Google Books API")
                            }
                        }
                    } else {
                        showIsbnHelpDialog = true
                        Logger.d("ScanISBN", "Nessun ISBN EAN-13 valido trovato.")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "${context.getString(R.string.error_prefix)} ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Logger.e("ScanISBN", "Errore scansione barcode: ${it.message}")
                }
        } else {
            Toast.makeText(context, context.getString(R.string.no_image_captured), Toast.LENGTH_SHORT).show()
            Logger.d("ScanISBN", "Bitmap nullo ricevuto dal TakePicturePreview.")
        }
    }
    val scrollState = rememberScrollState()
    fun saveBook(book: Map<String, Any>) {
        db.collection("books")
            .add(book)
            .addOnSuccessListener {
                Toast.makeText(context, context.getString(R.string.book_added), Toast.LENGTH_SHORT)
                    .show()
                title = ""
                author = ""
                publisher = ""
                genre = ""
                language = ""
                publishDate = ""
                description = ""
                pageCount = ""
                selectedFormatKey = "physical"
                selectedReadingKey = "not_started"
                rating = ""
                notes = ""
                coverUrl = ""
                location = ""
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_prefix) + " ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            Logger.d("PhotoPicker", "Nessuna immagine selezionata")
            return@rememberLauncherForActivityResult
        }

        Logger.d("PhotoPicker", "URI selezionato: $uri")
        uploadingCover = true
        coroutineScope.launch {
            try {
                val downloadUrl = uploadCompressedImage(
                    context = context,
                    imageUri = uri,
                    userId = userId,
                    folder = "covers"
                )
                coverUrl = downloadUrl
                Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("crash in addBook con fotocamera")
                FirebaseCrashlytics.getInstance().recordException(e)
                Logger.e("PhotoPicker", "Errore upload: ${e.message}", e)
                Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()


                val sw = java.io.StringWriter()
                errorStackTrace = sw.toString()
                showErrorDialog = true
                Logger.e("DEBUG_DIALOG", "Errore stacktrace", e)


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

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
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
                        userId = userId,
                        folder = "covers"
                    )
                    coverUrl = downloadUrl
                    Toast.makeText(context, context.getString(R.string.cover_uploaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crash in AddBookScreen")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(context, context.getString(R.string.upload_failed, e.message ?: ""), Toast.LENGTH_LONG).show()


                    val sw = java.io.StringWriter()
                    errorStackTrace = sw.toString()
                    showErrorDialog = true
                    Logger.e("DEBUG_DIALOG", "Errore stacktrace", e)



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

    LaunchedEffect(scannedIsbn) {
        scannedIsbn?.let {
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedIsbn")

            val book = fetchBookInfoFromGoogleBooks(it, BuildConfig.GOOGLE_BOOKS_API_KEY)
            if (book != null) {
                title = book.title
                author = book.authors.joinToString(", ") { a -> a.name }
                genre = book.genre
                publishDate = book.publishDate
                publisher = book.publisher
                language = book.language
                description = book.description
                pageCount = if (book.pageCount > 0) book.pageCount.toString() else ""
                rating = if (book.averageRating > 0.0) book.averageRating.toInt().toString() else ""
                coverUrl = book.coverUrl
                location = book.location

                Toast.makeText(context, context.getString(R.string.text_recognized), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.no_book_found), Toast.LENGTH_SHORT).show()
            }
        }
    }



    Column(modifier = Modifier.fillMaxSize()) {
        // 🔼 Bottoni fissi in alto
        Surface(shadowElevation = 4.dp) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    /*
                    Button(
                        onClick = {
                            if (cameraPermissionGranted) barcodeScannerLauncher.launch(null)
                            else permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                    ) {
                        Text(stringResource(R.string.scan_isbn), color = Color.White)
                    }

                     */
                    Button(
                        onClick = {
                            navController.navigate("scan_isbn_live")
                        },modifier = Modifier.weight(1f),
                          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                    )  {
                            Text(stringResource(R.string.scan_isbn), color = Color.White)
                        }

                    Button(
                        onClick = {
                            if (isLimitReached) {
                                showLimitDialog = true
                                return@Button
                            }
                            if (title.isBlank()) {
                                Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val book = hashMapOf(
                                "title" to title,
                                "author" to author,
                                "genre" to genre,
                                "publishDate" to publishDate,
                                "userId" to userId,
                                "language" to language,
                                "description" to description,
                                "pageCount" to (pageCount.toIntOrNull() ?: 0),
                                "format" to selectedFormatKey,
                                "readingStatus" to selectedReadingKey,
                                "addedDate" to addedDate,
                                "rating" to rating.trim(),
                                "notes" to notes,
                                "coverUrl" to coverUrl,
                                "location" to location
                            ).apply {
                                if (selectedReadingStatus.lowercase() == context.getString(R.string.status_completed).lowercase()) {
                                    this["readDate"] = currentDate
                                }
                            }


                            db.collection("books")
                                .whereEqualTo("userId", userId)
                                .whereEqualTo("title", title)
                                .whereEqualTo("author", author)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (!snapshot.isEmpty) {
                                        pendingBookData = book
                                        showDuplicateDialog = true
                                    } else {
                                        saveBook(book)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "$errorPrefix ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank()
                    ) {
                        Text(saveBookLabel)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState), // Scroll abilitato
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
                onValueChange = {},
                label = { Text(stringResource(R.string.publish_date), fontSize = 14.sp) },
                textStyle = bookFieldTextStyle,
                modifier = bookFieldModifier,
                readOnly = true,
                enabled = false
            )

            Button(onClick = { showDatePicker = true }) {
                Text(stringResource(R.string.select_date))
            }

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
                    value = stringResourceByName("format_$selectedFormatKey"),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.format)) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedFormat,
                    onDismissRequest = { expandedFormat = false }) {
                    formatOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedFormatKey = key
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
                    value = stringResourceByName("status_$selectedReadingKey"),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.reading_status)) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedReading,
                    onDismissRequest = { expandedReading = false }) {
                    readingStatusOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedReadingKey = key
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
                            errorStackTrace = sw.toString()
                            showErrorDialog = true
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
                            errorStackTrace = sw.toString()
                            showErrorDialog = true
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
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    title = { Text("Tecnical error") },
                    text = {
                        Box(modifier = Modifier
                            .heightIn(min = 100.dp, max = 400.dp)
                            .verticalScroll(rememberScrollState())) {
                            Text(errorStackTrace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                )
            }


        }

        if (showDuplicateDialog && pendingBookData != null) {
            AlertDialog(
                onDismissRequest = { showDuplicateDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        saveBook(pendingBookData!!)
                        showDuplicateDialog = false
                        pendingBookData = null
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDuplicateDialog = false
                        pendingBookData = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.duplicate_book_title)) },
                text = { Text(stringResource(R.string.duplicate_book_message)) }
            )
        }
    }
    if (showIsbnHelpDialog) {
        AlertDialog(
            onDismissRequest = { showIsbnHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showIsbnHelpDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.isbn_not_found_dialog_title)) },
            text = { Text(stringResource(R.string.isbn_not_found_dialog_message)) }
        )
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
