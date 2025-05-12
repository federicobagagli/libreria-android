package com.federico.mylibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.viewmodel.BookFilterState
import com.federico.mylibrary.viewmodel.LibraryFilterViewModel

@Composable
fun ViewLibraryScreen(navController: NavController, filterViewModel: LibraryFilterViewModel) {
    var filters by remember { mutableStateOf(BookFilterState()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pulsanti in alto
        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    filterViewModel.clearFilters()
                    navController.navigate("books")
                }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.view_all_books))
                }

                Button(onClick = {
                    filterViewModel.updateFilters(filters)
                    navController.navigate("books")
                }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.view_by_criteria))
                }

                Button(
                    onClick = { navController.navigate("add") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.add_book), color = Color.White)
                }
            }
        }

        // Filtri scrollabili
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fun Modifier.full() = this.fillMaxWidth()
            OutlinedTextField(filters.title, { filters = filters.copy(title = it) }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.full())
            OutlinedTextField(filters.author, { filters = filters.copy(author = it) }, label = { Text(stringResource(R.string.author)) }, modifier = Modifier.full())
            OutlinedTextField(filters.publisher, { filters = filters.copy(publisher = it) }, label = { Text(stringResource(R.string.book_publisher)) }, modifier = Modifier.full())
            OutlinedTextField(filters.genre, { filters = filters.copy(genre = it) }, label = { Text(stringResource(R.string.genre)) }, modifier = Modifier.full())
            OutlinedTextField(filters.language, { filters = filters.copy(language = it) }, label = { Text(stringResource(R.string.book_language)) }, modifier = Modifier.full())
            OutlinedTextField(filters.publishDate, { filters = filters.copy(publishDate = it) }, label = { Text(stringResource(R.string.publish_date)) }, modifier = Modifier.full())
            OutlinedTextField(filters.description, { filters = filters.copy(description = it) }, label = { Text(stringResource(R.string.book_description)) }, modifier = Modifier.full())
            OutlinedTextField(filters.pageCount, { filters = filters.copy(pageCount = it) }, label = { Text(stringResource(R.string.book_page_count)) }, modifier = Modifier.full())
            OutlinedTextField(filters.format, { filters = filters.copy(format = it) }, label = { Text(stringResource(R.string.format)) }, modifier = Modifier.full())
            OutlinedTextField(filters.readingStatus, { filters = filters.copy(readingStatus = it) }, label = { Text(stringResource(R.string.reading_status)) }, modifier = Modifier.full())
            OutlinedTextField(filters.rating, { filters = filters.copy(rating = it) }, label = { Text(stringResource(R.string.book_rating)) }, modifier = Modifier.full())
            OutlinedTextField(filters.notes, { filters = filters.copy(notes = it) }, label = { Text(stringResource(R.string.book_notes)) }, modifier = Modifier.full())
            OutlinedTextField(filters.coverUrl, { filters = filters.copy(coverUrl = it) }, label = { Text(stringResource(R.string.book_cover_url)) }, modifier = Modifier.full())
        }
    }
}