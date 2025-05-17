package com.federico.mylibrary.debug

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.MainActivity

class CrashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stackTrace = getSharedPreferences("crash_log", Context.MODE_PRIVATE)
            .getString("last_crash", "Nessun log disponibile") ?: ""

        setContent {
            MaterialTheme {
                CrashDialog(stackTrace = stackTrace) {
                    // Riavvia l'app
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun CrashDialog(stackTrace: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text("Crash rilevato") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stackTrace, style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
