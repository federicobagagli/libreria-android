package com.federico.mylibrary.util

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


fun logCheckpoint(context: Context, tag: String, throwable: Throwable? = null) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val stack = throwable?.stackTraceToString().orEmpty()

    // ✅ Log locale (visibile in Logcat)
    Log.d("LOG_CHECKPOINT", "📍 [$tag] - user=$userId")
    if (stack.isNotBlank()) Log.d("LOG_CHECKPOINT", stack)

    // ✅ Toast locale per debugging visivo (opzionale)
    Toast.makeText(context, "📍 $tag", Toast.LENGTH_SHORT).show()

    if (userId == null) {
        Log.w("LOG_CHECKPOINT", "❌ Impossibile salvare log: utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()
    val entry = hashMapOf(
        "tag" to tag,
        "timestamp" to System.currentTimeMillis(),
        "stacktrace" to stack,
        "device" to Build.MODEL,
        "android" to Build.VERSION.SDK_INT
    )

    db.collection("crash_trace")
        .document(userId)
        .collection("log")
        .add(entry)
        .addOnSuccessListener {
            Log.d("LOG_CHECKPOINT", "✅ Log salvato su Firestore: $tag")
        }
        .addOnFailureListener {
            Log.e("LOG_CHECKPOINT", "❌ Fallito salvataggio log: ${it.message}")
        }
}