package com.federico.mylibrary.backup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    var showRestoreLibraryDialog by remember { mutableStateOf(false) }
    var showRestoreRecordsDialog by remember { mutableStateOf(false) }
    var showRestoreMoviesDialog by remember { mutableStateOf(false) }

    var isRestoring by remember { mutableStateOf(false) }

    val restoreLibrarySuccess = stringResource(R.string.restore_library_success)
    val restoreLibraryFailed = stringResource(R.string.restore_library_failed)
    val restoreRecordSuccess = stringResource(R.string.restore_records_success)
    val restoreRecordFailed = stringResource(R.string.restore_records_failed)
    val restoreMovieSuccess = stringResource(R.string.restore_movies_success)
    val restoreMovieFailed = stringResource(R.string.restore_movies_failed)

    var lastLibraryBackup by remember { mutableStateOf<Long?>(null) }
    var lastRecordBackup by remember { mutableStateOf<Long?>(null) }
    var lastMovieBackup by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            lastLibraryBackup = BackupUtils.getBackupTimestamp(context, "library")
            lastRecordBackup = BackupUtils.getBackupTimestamp(context, "record")
            lastMovieBackup = BackupUtils.getBackupTimestamp(context, "movie")
        }
    }

    fun formatTimestamp(timestamp: Long?): String {
        return timestamp?.let {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        } ?: "-"
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(scrollState)
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
        Text(
            text = stringResource(R.string.last_backup_time, formatTimestamp(lastLibraryBackup)),
            style = MaterialTheme.typography.bodySmall
        )

        Button(
            onClick = { showRestoreLibraryDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestoring
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
        Text(
            text = stringResource(R.string.last_backup_time, formatTimestamp(lastRecordBackup)),
            style = MaterialTheme.typography.bodySmall
        )

        Button(
            onClick = { showRestoreRecordsDialog = true },
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
        Text(
            text = stringResource(R.string.last_backup_time, formatTimestamp(lastMovieBackup)),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = { showRestoreMoviesDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRestoring
        ) {
            Text("♻️ " + stringResource(R.string.restore_backup_movies))
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

        if (isRestoring) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // 📚 Conferma ripristino libreria
    if (showRestoreLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreLibraryDialog = false },
            title = { Text(stringResource(R.string.confirm_restore_title)) },
            text = { Text(stringResource(R.string.confirm_restore_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreLibraryDialog = false
                    isRestoring = true
                    scope.launch {
                        val success = BackupUtils.restoreLibraryBackup(context)
                        isRestoring = false

                        Toast.makeText(
                            context,
                            if (success) restoreLibrarySuccess
                            else restoreLibraryFailed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreLibraryDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    // 💿 Conferma ripristino discoteca
    if (showRestoreRecordsDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreRecordsDialog = false },
            title = { Text(stringResource(R.string.confirm_restore_title)) },
            text = { Text(stringResource(R.string.confirm_restore_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreRecordsDialog = false
                    isRestoring = true
                    scope.launch {
                        val success = BackupUtils.restoreRecordBackup(context)
                        isRestoring = false
                        Toast.makeText(
                            context,
                            if (success) restoreRecordSuccess
                            else restoreRecordFailed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreRecordsDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    // 🎬 Conferma ripristino cineteca
    if (showRestoreMoviesDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreMoviesDialog = false },
            title = { Text(stringResource(R.string.confirm_restore_title)) },
            text = { Text(stringResource(R.string.confirm_restore_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreMoviesDialog = false
                    isRestoring = true
                    scope.launch {
                        val success = BackupUtils.restoreMovieBackup(context)
                        isRestoring = false
                        Toast.makeText(
                            context,
                            if (success) restoreMovieSuccess
                            else restoreMovieFailed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreMoviesDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}
