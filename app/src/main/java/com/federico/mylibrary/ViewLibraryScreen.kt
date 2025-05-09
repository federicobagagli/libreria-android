package com.federico.mylibrary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun ViewLibraryScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

        Button(onClick = {
            navController.navigate("books/_/_/_/_")
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.view_all_books))
        }

        Button(
            onClick = {
                val encodedTitle = title.ifBlank { "_" }
                val encodedAuthor = author.ifBlank { "_" }
                val encodedGenre = genre.ifBlank { "_" }
                val encodedDate = publishDate.ifBlank { "_" }
                navController.navigate("books/$encodedTitle/$encodedAuthor/$encodedGenre/$encodedDate")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_by_criteria))
        }

        Button(
            onClick = { navController.navigate("add") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_book), color = Color.White)
        }

    }
}
