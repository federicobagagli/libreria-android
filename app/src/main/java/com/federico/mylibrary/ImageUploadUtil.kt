package com.federico.mylibrary

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import java.io.File

suspend fun uploadCompressedImage(
    context: Context,
    imageUri: Uri,
    userId: String,
    folder: String = "covers",
    maxWidth: Int = 600,
    maxHeight: Int = 900,
    quality: Int = 75
): String {
    // ✅ Gestione permesso persistente per Android 13/14+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        try {
            context.contentResolver.takePersistableUriPermission(
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.w("ImageUpload", "Permesso URI non acquisito: ${e.message}")
            // Continua comunque, a volte non è necessario
        } catch (e: Exception) {
            Log.e("ImageUpload", "Errore imprevisto URI: ${e.message}")
        }
    }

    val bitmap = withContext(Dispatchers.IO) {
        try {
            val original = if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                Log.d("ImageUpload", "ImageDecoder source OK")
                ImageDecoder.decodeBitmap(source)
            } else {
                Log.d("ImageUpload", "Using MediaStore")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
            Log.d("ImageUpload", "Bitmap OK, scalatura in corso")
            original.scale(maxWidth, maxHeight)
        } catch (e: SecurityException) {
            Log.e("ImageUpload", "SecurityException: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ImageUpload", "Errore bitmap: ${e.message}", e)
            throw e
        }
    }


    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    val data = baos.toByteArray()

    val filename = "$folder/$userId/${System.currentTimeMillis()}.jpg"

    val storageRef = FirebaseStorage.getInstance().reference.child(filename)

    storageRef.putBytes(data).await()
    return storageRef.downloadUrl.await().toString()
}

fun createTempImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "temp_photo.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}