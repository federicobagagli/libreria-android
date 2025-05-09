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
