package com.federico.mylibrary.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import com.federico.mylibrary.R

@Composable
fun rememberMediaPermissionLauncher(
    context: Context,
    onGranted: () -> Unit,
    onDenied: () -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 🔁 Ritarda un attimo l'esecuzione per sicurezza
            Handler(Looper.getMainLooper()).post {
                onGranted()
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                onDenied()
                Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }
}