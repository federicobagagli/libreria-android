package com.federico.mylibrary.backup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import kotlinx.coroutines.launch


@Composable
fun BackupScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRestoring by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

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
                scope.launch { BackupUtils.backupLibrary(context) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📚 " + stringResource(R.string.backup_generate_library))
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("♻️ " + stringResource(R.string.restore_backup_library))
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Button(
            onClick = {
                scope.launch { BackupUtils.backupRecords(context) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💿 " + stringResource(R.string.backup_generate_records))
        }

        Button(
            onClick = {
                isRestoring = true
                scope.launch {
                    val success = BackupUtils.restoreRecordBackup(context)
                    isRestoring = false
                    Toast.makeText(
                        context,
                        if (success) context.getString(R.string.restore_records_success)
                        else context.getString(R.string.restore_records_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestoring
        ) {
            Text("♻️ " + stringResource(R.string.restore_backup_records))
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Button(
            onClick = {
                scope.launch { BackupUtils.backupMovies(context) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎬 " + stringResource(R.string.backup_generate_movies))
        }

        Button(
            onClick = {
                isRestoring = true
                scope.launch {
                    val success = BackupUtils.restoreMovieBackup(context)
                    isRestoring = false
                    Toast.makeText(
                        context,
                        if (success) context.getString(R.string.restore_movies_success)
                        else context.getString(R.string.restore_movies_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestoring
        ) {
            Text("♻️ " + stringResource(R.string.restore_backup_movies))
        }

        if (isRestoring) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    isRestoring = true
                    scope.launch {
                        val success = BackupUtils.restoreLibraryBackup(context)
                        isRestoring = false
                        Toast.makeText(
                            context,
                            if (success) context.getString(R.string.restore_library_success)
                            else context.getString(R.string.restore_library_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_restore_title)) },
            text = { Text(stringResource(R.string.confirm_restore_message)) }
        )
    }
}
