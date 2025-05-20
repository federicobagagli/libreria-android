package com.federico.mylibrary.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.federico.mylibrary.R

@Composable
fun LimitReachedDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onGoPremium: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onGoPremium) {
                    Text(stringResource(R.string.go_premium))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.limit_reached_title)) },
            text = { Text(stringResource(R.string.limit_reached_message)) }
        )
    }
}
