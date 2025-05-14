package com.federico.mylibrary.model

import kotlinx.serialization.Serializable

@Serializable
data class Record(
    val title: String = "",
    val artist: String = "",
    val format: String = "",
    val year: String = "",
    val genre: String = "",
    val physicalSupport: Boolean = false,
    val type: String = "Track", // "Track" or "Album"

    val trackNumber: Int? = null,
    val album: String? = null,
    val duration: String? = null,
    val label: String? = null,
    val soloists: String? = null,

    val tracklist: List<String>? = null,
    val totalTracks: Int? = null,
    val multiAlbum: Boolean = false,

    val language: String = "",
    val description: String = "",
    val rating: String = "",
    val notes: String = "",
    val coverUrl: String = "",
    val addedDate: String = "",
    val location: String = "",

    val userId: String = ""
)
