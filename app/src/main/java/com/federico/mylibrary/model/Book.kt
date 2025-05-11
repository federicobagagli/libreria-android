package com.federico.mylibrary.model

@kotlinx.serialization.Serializable
data class Book(
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val publishDate: String = "",
    val userId: String = "" // ✅ richiesto per Firestore + backup
)
