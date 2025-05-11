package com.federico.mylibrary.backup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import kotlinx.coroutines.launch
import com.federico.mylibrary.backup.BackupUtils
import kotlinx.coroutines.launch

@Composable
fun BackupScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_section_title),
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                scope.launch {
                    BackupUtils.backupLibrary(context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.backup_generate_library))
        }

        Button(
            onClick = {
                // Azione per ripristinare il backup (da Firebase Storage)
                // Da collegare a funzione dedicata
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.backup_restore_library))
        }
    }
}
