package com.federico.mylibrary.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.federico.mylibrary.R

@Composable
fun TmdbMoviePickerDialog(
    movies: List<MovieInfoLite>,
    onSelect: (MovieInfoLite) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(stringResource(R.string.select_movie_title)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(movies) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(movie) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (movie.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = movie.coverUrl,
                                contentDescription = movie.title,
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Spacer(modifier = Modifier.size(50.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = movie.title, fontWeight = FontWeight.Bold)
                            Text(text = movie.releaseDate, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
