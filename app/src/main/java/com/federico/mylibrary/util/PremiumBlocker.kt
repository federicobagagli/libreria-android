package com.federico.mylibrary.util

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.federico.mylibrary.R

@Composable
fun PremiumBlocker(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onClickAllowed: () -> Unit,
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.premium_required_title)) },
            text = { Text(stringResource(R.string.premium_required_message)) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Button(
        onClick = {
            if (isPremium) onClickAllowed() else showDialog = true
        },
        modifier = modifier
    ) {
        content()
    }
}
