package com.federico.mylibrary.model

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String = "",
    val title: String = "",
    val originalTitle: String = "",
    val director: String = "",
    val cast: String = "",
    val productionCompany: String = "",
    val genre: String = "",
    val language: String = "",
    val publishDate: String = "",
    val description: String = "",
    val duration: Int = 0,
    val format: String = "",
    val addedDate: String = "",
    val rating: String = "",
    val notes: String = "",
    val userId: String = "",
    val location: String = "",
    val coverUrl: String = ""
)
