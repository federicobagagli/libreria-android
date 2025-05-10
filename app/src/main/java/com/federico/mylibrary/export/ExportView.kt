package com.federico.mylibrary.export


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.federico.mylibrary.R
import java.io.File
import java.io.FileOutputStream
import com.federico.mylibrary.export.CsvExporter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import java.io.OutputStream

@Composable
fun ExportView(
    items: List<ExportableItem>,
    fileName: String
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val headers = remember { BookExportItem.headers(context) }

    val rows = items.map { it.toExportRow() }
    val csvContent = remember(items) { CsvExporter.generateCsvContent(headers, rows) }

    var savedFilePath by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Text(text = stringResource(R.string.export_title), style = MaterialTheme.typography.titleLarge)

        Button(onClick = {
            val file = exportCsvToDownloadsUsingMediaStore(context, csvContent, fileName)
            if (file != null) {
                savedFilePath = file.absolutePath
                showDialog = true
            } else {
                Toast.makeText(context, context.getString(R.string.export_error), Toast.LENGTH_SHORT).show()
            }

        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.export_download))
        }

        Button(onClick = {
            val file = exportToCsvFile(context, csvContent, fileName)
            file?.let {
                shareFileViaIntent(context, it, "text/csv")
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.export_share))
        }

    }

    if (showDialog && savedFilePath != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text(stringResource(R.string.export_success)) },
            text = {
                SelectionContainer {
                    Text(
                        text = savedFilePath ?: "",
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    clipboardManager.setText(AnnotatedString(savedFilePath ?: ""))
                                    Toast.makeText(context, context.getString(R.string.export_copied), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }
            }
        )
    }

}

fun exportToCsvFile(context: Context, content: String, fileName: String): File? {
    return try {
        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (exportDir?.exists() == false) exportDir.mkdirs()
        val file = File(exportDir, fileName)
        FileOutputStream(file).use {
            it.write(content.toByteArray())
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun shareFileViaIntent(context: Context, file: File, mimeType: String) {
    val uri: Uri = FileProvider.getUriForFile(context, "com.federico.mylibrary.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, null))
}


fun exportCsvToDownloadsUsingMediaStore(context: Context, content: String, fileName: String): File? {
    val resolver = context.contentResolver
    val csvCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Files.getContentUri("external")
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    resolver.delete(csvCollection, "${MediaStore.MediaColumns.DISPLAY_NAME}=?", arrayOf(fileName))
    val fileUri = resolver.insert(csvCollection, contentValues)

    return if (fileUri != null) {
        resolver.openOutputStream(fileUri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            content.lines().forEach { line ->
                writer.write(line)
                writer.write("\r\n") // per compatibilità Excel
            }
        }



        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(fileUri, contentValues, null, null)

        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
    } else null
}