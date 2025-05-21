package com.federico.mylibrary.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Returns a localized string using the resource name.
 * Example: passing "status_reading" will return "Reading" or "In lettura" etc.
 */
@Composable
fun stringResourceByName(name: String): String {
    val context = LocalContext.current
    return stringResourceByName(name, context)
}

fun stringResourceByName(name: String, context: Context): String {
    val resId = context.resources.getIdentifier(name, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else name
}

