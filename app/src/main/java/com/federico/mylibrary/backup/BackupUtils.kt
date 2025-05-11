package com.federico.mylibrary.backup

import android.content.Context
import android.widget.Toast
import com.federico.mylibrary.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupUtils {

    suspend fun backupLibrary(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Utente non autenticato", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val books = snapshot.documents.mapNotNull { it.toObject<Book>() }

            val jsonString = Json.encodeToString(books)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val storageRef = FirebaseStorage.getInstance().reference
                .child("backups/$userId/library_backup.json")

            storageRef.putBytes(jsonBytes).await()

            Toast.makeText(context, "✅ Backup completato", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "❌ Errore durante il backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    suspend fun restoreLibrary(context: Context) {
        // Prossimamente
    }
}
