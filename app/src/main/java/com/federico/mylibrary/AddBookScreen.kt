package com.federico.mylibrary

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat



@Composable
fun AddBookScreen() {
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
                    val allText = visionText.text.lowercase()

                    // Semplice logica di estrazione (puoi migliorare con regex o IA)
                    title = visionText.textBlocks.firstOrNull()?.text?.take(50) ?: title
                    author = visionText.textBlocks.getOrNull(1)?.text?.take(50) ?: author

                    Toast.makeText(context, context.getString(R.string.text_recognized), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, context.getString(R.string.error_prefix) + " ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }




    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text(stringResource(R.string.author)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text(stringResource(R.string.genre)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = publishDate,
            onValueChange = { publishDate = it },
            label = { Text(stringResource(R.string.publish_date)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (cameraPermissionGranted) {
                    cameraLauncher.launch()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
        ) {
            Text(stringResource(R.string.add_with_camera), color = Color.White)
        }

        val barcodeScannerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                val image = InputImage.fromBitmap(bitmap, 0)

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val isbnBarcode = barcodes.firstOrNull { it.rawValue?.length == 13 && it.format == com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 }
                        val isbn = isbnBarcode?.rawValue

                        if (isbn != null) {
                            Toast.makeText(context, "ISBN: $isbn", Toast.LENGTH_SHORT).show()

                            // TODO: chiamata a OpenLibrary o Google Books API con ISBN
                        } else {
                            Toast.makeText(context, context.getString(R.string.no_isbn_found), Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "${context.getString(R.string.error_prefix)} ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        Button(
            onClick = {
                if (cameraPermissionGranted) {
                    barcodeScannerLauncher.launch()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
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
