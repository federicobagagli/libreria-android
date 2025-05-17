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
    folder: String,  //folder: String = "covers",
    maxWidth: Int = 600,
    maxHeight: Int = 900,
    quality: Int = 75
): String {
    // 👉 Applica solo per URI persistibili (es. da galleria), non FileProvider
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
        imageUri.scheme == "content" &&
        imageUri.authority != "${context.packageName}.fileprovider"
    ) {
        try {
            context.contentResolver.takePersistableUriPermission(
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.w("ImageUpload", "Persistable permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e("ImageUpload", "Unexpected URI error: ${e.message}")
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
            Log.d("ImageUpload", "Bitmap decoded, resizing...")
            original.scale(maxWidth, maxHeight)
        } catch (e: SecurityException) {
            Log.e("ImageUpload", "SecurityException: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("ImageUpload", "Bitmap error: ${e.message}", e)
            throw e
        }
    }

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    val data = baos.toByteArray()

    val filename = "$folder/$userId/${System.currentTimeMillis()}.jpg"
    val storageRef = FirebaseStorage.getInstance().reference.child(filename)

    Log.d("ImageUpload", "Uploading image to Firebase Storage...")
    storageRef.putBytes(data).await()
    val downloadUrl = storageRef.downloadUrl.await().toString()
    Log.d("ImageUpload", "Upload complete. URL: $downloadUrl")
    return downloadUrl
}


fun createTempImageUri(context: Context): Uri {
    val file = File(context.getExternalFilesDir(null), "camera_temp_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

fun createMediaStoreImageUri(context: Context): Uri? {
    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "cover_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyLibrary")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val contentResolver = context.contentResolver
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    return uri
}
