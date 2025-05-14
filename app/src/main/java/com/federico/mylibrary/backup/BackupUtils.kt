package com.federico.mylibrary.backup

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Book
import com.federico.mylibrary.model.Record
import com.federico.mylibrary.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

object BackupUtils {

    // 📚 LIBRARY
    suspend fun backupLibrary(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, context.getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
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

            Toast.makeText(context, context.getString(R.string.backup_library_success), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.backup_library_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    suspend fun restoreLibraryBackup(context: Context): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return false
            val fileRef = FirebaseStorage.getInstance().reference
                .child("backups/${user.uid}/library_backup.json")

            val localFile = File(context.cacheDir, "temp_library_backup.json")
            fileRef.getFile(localFile).await()

            val jsonContent = localFile.readText()
            val books: List<Book> = Json.decodeFromString(jsonContent)

            val firestore = FirebaseFirestore.getInstance()
            val existing = firestore.collection("books")
                .whereEqualTo("userId", user.uid)
                .get().await()

            for (doc in existing.documents) doc.reference.delete().await()
            for (book in books) firestore.collection("books").add(book).await()

            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Restore library failed: ${e.message}", e)
            false
        }
    }

    // 💿 RECORDS
    suspend fun backupRecords(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, context.getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("records")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { it.toObject<Record>() }
            val jsonString = Json.encodeToString(records)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val storageRef = FirebaseStorage.getInstance().reference
                .child("backups/$userId/record_backup.json")

            storageRef.putBytes(jsonBytes).await()
            Toast.makeText(context, context.getString(R.string.backup_records_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.backup_records_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    suspend fun restoreRecordBackup(context: Context): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return false
            val fileRef = FirebaseStorage.getInstance().reference
                .child("backups/${user.uid}/record_backup.json")

            val localFile = File(context.cacheDir, "temp_record_backup.json")
            fileRef.getFile(localFile).await()

            val jsonContent = localFile.readText()
            val records: List<Record> = Json.decodeFromString(jsonContent)

            val firestore = FirebaseFirestore.getInstance()
            val existing = firestore.collection("records")
                .whereEqualTo("userId", user.uid)
                .get().await()

            for (doc in existing.documents) doc.reference.delete().await()
            for (record in records) firestore.collection("records").add(record).await()

            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Restore records failed: ${e.message}", e)
            false
        }
    }

    // 🎬 MOVIES
    suspend fun backupMovies(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, context.getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("movies")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val movies = snapshot.documents.mapNotNull { it.toObject<Movie>() }
            val jsonString = Json.encodeToString(movies)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val storageRef = FirebaseStorage.getInstance().reference
                .child("backups/$userId/movie_backup.json")

            storageRef.putBytes(jsonBytes).await()
            Toast.makeText(context, context.getString(R.string.backup_movies_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.backup_movies_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    suspend fun restoreMovieBackup(context: Context): Boolean {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return false
            val fileRef = FirebaseStorage.getInstance().reference
                .child("backups/${user.uid}/movie_backup.json")

            val localFile = File(context.cacheDir, "temp_movie_backup.json")
            fileRef.getFile(localFile).await()

            val jsonContent = localFile.readText()
            val movies: List<Movie> = Json.decodeFromString(jsonContent)

            val firestore = FirebaseFirestore.getInstance()
            val existing = firestore.collection("movies")
                .whereEqualTo("userId", user.uid)
                .get().await()

            for (doc in existing.documents) doc.reference.delete().await()
            for (movie in movies) firestore.collection("movies").add(movie).await()

            true
        } catch (e: Exception) {
            Log.e("BackupUtils", "Restore movies failed: ${e.message}", e)
            false
        }
    }

    suspend fun getBackupTimestamp(context: Context, type: String): Long? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            val ref = FirebaseStorage.getInstance().reference
                .child("backups/$userId/${type}_backup.json")
            ref.metadata.await().updatedTimeMillis
        } catch (e: Exception) {
            null
        }
    }
}
