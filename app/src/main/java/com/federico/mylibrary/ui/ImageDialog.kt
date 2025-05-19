package com.federico.mylibrary.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.federico.mylibrary.R
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

@Composable
fun ImageDialog(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    if (imageUrl != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            },
            text = {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.book_cover_url),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .padding(8.dp)
                )
            }
        )
    }
}
