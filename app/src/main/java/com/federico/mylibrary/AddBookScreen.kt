package com.federico.mylibrary

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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

@Serializable
data class BookInfo(
    val title: String = "",
    val authors: List<AuthorInfo> = emptyList(),
    val genre: String = "",
    val publishDate: String = ""
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

        val items = response["items"]?.jsonArray ?: return null
        val volumeInfo = items[0].jsonObject["volumeInfo"]?.jsonObject ?: return null

        val title = volumeInfo["title"]?.jsonPrimitive?.content ?: ""
        val authorsArray = volumeInfo["authors"]?.jsonArray?.mapNotNull {
            it.jsonPrimitive.contentOrNull
        } ?: emptyList()

        val genre = volumeInfo["categories"]?.jsonArray
            ?.firstOrNull()?.jsonPrimitive?.content ?: ""

        val publishDate = volumeInfo["publishedDate"]?.jsonPrimitive?.content ?: ""

        BookInfo(
            title = title,
            authors = authorsArray.map { AuthorInfo(it) },
            genre = genre,
            publishDate = publishDate
        )
    } catch (e: Exception) {
        null
    } finally {
        client.close()
    }
}

@Composable
fun AddBookScreen() {
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }

    val saveBookLabel = stringResource(R.string.save_book)
    val bookAdded = stringResource(R.string.book_added)
    val errorPrefix = stringResource(R.string.error_prefix)
    val missingTitle = stringResource(R.string.missing_title)

    var cameraPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val options = TextRecognizerOptions.Builder().build()
            val recognizer = TextRecognition.getClient(options)
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText: Text ->
                    title = visionText.textBlocks.firstOrNull()?.text?.take(50) ?: title
                    author = visionText.textBlocks.getOrNull(1)?.text?.take(50) ?: author
                    Toast.makeText(context, context.getString(R.string.text_recognized), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, context.getString(R.string.error_prefix) + " ${it.message}", Toast.LENGTH_SHORT).show()
                }
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

            Toast.makeText(context, "Elaborazione immagine...", Toast.LENGTH_SHORT).show()
            Log.d("ScanISBN", "Elaborazione immagine per barcode...")

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    Log.d("ScanISBN", "Barcode trovati: ${barcodes.size}")

                    val isbnBarcode = barcodes.firstOrNull {
                        Toast.makeText(context, "Rilevato: ${it.rawValue}", Toast.LENGTH_SHORT).show()
                        it.rawValue?.length == 13 && it.format == Barcode.FORMAT_EAN_13
                    }

                    val isbn = isbnBarcode?.rawValue

                    if (isbn != null) {
                        Toast.makeText(context, "ISBN trovato: $isbn", Toast.LENGTH_SHORT).show()
                        Log.d("ScanISBN", "ISBN valido: $isbn")

                        coroutineScope.launch {
                            val book = fetchBookInfoFromGoogleBooks(isbn, "AIzaSyCV2y1_3wYmMuAsgRyu-c5VO3oGCsg8bDo")
                            if (book != null) {
                                title = book.title
                                author = book.authors.joinToString(", ") { it.name }
                                genre = book.genre
                                publishDate = book.publishDate
                                Toast.makeText(context, context.getString(R.string.text_recognized), Toast.LENGTH_SHORT).show()
                                Log.d("ScanISBN", "Libro trovato: ${book.title} - ${book.authors.joinToString(", ") { it.name }}")
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_book_found), Toast.LENGTH_SHORT).show()
                                Log.d("ScanISBN", "Nessun libro trovato via Google Books API")
                            }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.no_isbn_found), Toast.LENGTH_SHORT).show()
                        Log.d("ScanISBN", "Nessun ISBN EAN-13 valido trovato.")
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "${context.getString(R.string.error_prefix)} ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ScanISBN", "Errore scansione barcode: ${it.message}")
                }
        } else {
            Toast.makeText(context, "Immagine non acquisita (bitmap null)", Toast.LENGTH_SHORT).show()
            Log.d("ScanISBN", "Bitmap nullo ricevuto dal TakePicturePreview.")
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text(stringResource(R.string.author)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text(stringResource(R.string.genre)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = publishDate, onValueChange = { publishDate = it }, label = { Text(stringResource(R.string.publish_date)) }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (cameraPermissionGranted) cameraLauncher.launch(null)
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
        ) {
            Text(stringResource(R.string.add_with_camera), color = Color.White)
        }

        Button(
            onClick = {
                if (cameraPermissionGranted) barcodeScannerLauncher.launch(null)
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
        ) {
            Text(stringResource(R.string.scan_isbn), color = Color.White)
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, missingTitle, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val book = hashMapOf(
                    "title" to title,
                    "author" to author,
                    "genre" to genre,
                    "publishDate" to publishDate,
                    "userId" to userId
                )
                db.collection("books")
                    .add(book)
                    .addOnSuccessListener {
                        Toast.makeText(context, bookAdded, Toast.LENGTH_SHORT).show()
                        title = ""; author = ""; genre = ""; publishDate = ""
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "$errorPrefix ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank()
        ) {
            Text(saveBookLabel)
        }
    }
}
