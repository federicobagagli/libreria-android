package com.federico.mylibrary.model

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val genre: String = "",
    val language: String = "",
    val publishDate: String = "",
    val description: String = "",
    val pageCount: Int = 0,
    val format: String = "",
    val readingStatus: String = "",
    val addedDate: String = "",
    val rating: String = "", // era Int
    val notes: String = "",
    val coverUrl: String = "",
    val userId: String = "",
    val location: String = ""
)
