package com.federico.mylibrary.util

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.federico.mylibrary.R

@Composable
fun PremiumBlocker(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    showDialogExternally: Boolean = false,
    onAllowed: @Composable () -> Unit
) {
    var showUpgradeDialog by remember { mutableStateOf(false) }

    if (isPremium) {
        onAllowed()
    } else {
        Button(
            onClick = { if (!showDialogExternally) showUpgradeDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = modifier
        ) {
            Text(stringResource(R.string.premium_required_title), color = MaterialTheme.colorScheme.onError)
        }
    }

    if (!showDialogExternally && showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text(stringResource(R.string.premium_required_title)) },
            text = { Text(stringResource(R.string.premium_required_message)) },
            confirmButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}
