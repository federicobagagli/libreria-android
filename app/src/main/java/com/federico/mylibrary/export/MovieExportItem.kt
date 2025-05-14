package com.federico.mylibrary.export

import android.content.Context
import com.federico.mylibrary.R

class MovieExportItem(
    private val title: String,
    private val originalTitle: String,
    private val director: String,
    private val cast: String,
    private val productionCompany: String,
    private val genre: String,
    private val language: String,
    private val description: String,
    private val publishDate: String,
    private val duration: Int,
    private val format: String,
    private val rating: String,
    private val notes: String,
    private val coverUrl: String,
    private val location: String
) : ExportableItem {

    override fun getExportHeaders(): List<String> = emptyList() // non usato

    override fun toExportRow(): List<String> = listOf(
        title, originalTitle, director, cast, productionCompany,
        genre, language, description, publishDate, duration.toString(),
        format, rating, notes, coverUrl, location
    )

    companion object {
        fun headers(context: Context): List<String> = listOf(
            context.getString(R.string.title),
            context.getString(R.string.original_title),
            context.getString(R.string.director),
            context.getString(R.string.cast),
            context.getString(R.string.production_company),
            context.getString(R.string.genre),
            context.getString(R.string.book_language),
            context.getString(R.string.book_description),
            context.getString(R.string.publish_date),
            context.getString(R.string.duration_minutes),
            context.getString(R.string.format),
            context.getString(R.string.book_rating),
            context.getString(R.string.book_notes),
            context.getString(R.string.book_cover_url),
            context.getString(R.string.book_location)
        )
    }
}
