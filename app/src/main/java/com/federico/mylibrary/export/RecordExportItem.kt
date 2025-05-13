
package com.federico.mylibrary.export

import android.content.Context
import com.federico.mylibrary.R

data class RecordExportItem(
    val title: String,
    val artist: String,
    val format: String,
    val year: String,
    val genre: String,
    val physicalSupport: Boolean,
    val type: String,
    val trackNumber: String,
    val album: String,
    val duration: String,
    val label: String,
    val soloists: String,
    val tracklist: String,
    val totalTracks: String,
    val multiAlbum: Boolean,
    val language: String,
    val description: String,
    val rating: String,
    val notes: String,
    val coverUrl: String,
    val addedDate: String,
    val location: String
) : ExportableItem {

    override fun getExportHeaders(): List<String> = listOf(
        title, artist, format, year, genre, physicalSupport.toString(), type,
        trackNumber, album, duration, label, soloists,
        tracklist, totalTracks, multiAlbum.toString(),
        language, description, rating, notes,
        coverUrl, addedDate, location
    )

    override fun toExportRow(): List<String> = listOf(
        title, artist, format, year, genre, physicalSupport.toString(), type,
        trackNumber, album, duration, label, soloists,
        tracklist, totalTracks, multiAlbum.toString(),
        language, description, rating, notes,
        coverUrl, addedDate, location
    )

    companion object {
        fun headers(context: Context): List<String> = listOf(
            context.getString(R.string.title),
            context.getString(R.string.artist),
            context.getString(R.string.format),
            context.getString(R.string.year),
            context.getString(R.string.genre),
            context.getString(R.string.physical_support),
            context.getString(R.string.type),
            context.getString(R.string.track_number),
            context.getString(R.string.album),
            context.getString(R.string.duration),
            context.getString(R.string.label),
            context.getString(R.string.soloists),
            context.getString(R.string.tracklist),
            context.getString(R.string.total_tracks),
            context.getString(R.string.multi_album),
            context.getString(R.string.language),
            context.getString(R.string.description),
            context.getString(R.string.rating),
            context.getString(R.string.notes),
            context.getString(R.string.cover_url),
            context.getString(R.string.added_date),
            context.getString(R.string.location)
        )
    }
}
