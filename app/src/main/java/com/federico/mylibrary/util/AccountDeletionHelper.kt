package com.federico.mylibrary.util

import android.content.Context
import android.widget.Toast
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


fun deleteUserAndData(context: Context, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return

    val userId = user.uid

    val collections = listOf("books", "records")

    collections.forEach { collection ->
        db.collection(collection)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    doc.reference.delete()
                }
            }
    }

    user.delete()
        .addOnSuccessListener {
            Toast.makeText(context, context.getString(R.string.account_deleted_success), Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, context.getString(R.string.account_deleted_error, it.message ?: ""), Toast.LENGTH_LONG).show()
        }
}
