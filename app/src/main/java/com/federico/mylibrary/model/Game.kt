package com.federico.mylibrary.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val title: String = "",
    val type: String = "",  // "board", "videogame", "altro"
    val platform: String = "",
    val publisher: String = "",
    val releaseDate: String = "",
    val genre: String = "",
    val language: String = "",
    val description: String = "",
    val minPlayers: Int = 0,
    val maxPlayers: Int = 0,
    val durationMinutes: Int = 0,
    val rating: String = "",
    val notes: String = "",
    val location: String = "",
    val addedDate: String = "",
    val userId: String = "",
    val coverUrl: String = ""
)
