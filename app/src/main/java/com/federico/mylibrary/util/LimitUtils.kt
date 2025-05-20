package com.federico.mylibrary.util

import com.google.firebase.firestore.FirebaseFirestore

fun checkLimitReached(
    userId: String,
    collection: String,
    limit: Int,
    onResult: (Boolean) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection(collection)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { snapshot ->
            onResult(snapshot.size() >= limit)
        }
        .addOnFailureListener {
            // In caso di errore, non blocchiamo ma mostriamo avviso
            onResult(false)
        }
}
