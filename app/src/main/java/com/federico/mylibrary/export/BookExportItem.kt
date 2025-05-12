package com.federico.mylibrary.export

import android.content.Context
import com.federico.mylibrary.R

class BookExportItem(
    private val title: String,
    private val author: String,
    private val publisher: String,
    private val genre: String,
    private val language: String,
    private val description: String,
    private val pageCount: Int,
    private val format: String,
    private val readingStatus: String,
    private val addedDate: String,
    private val rating: Int,
    private val notes: String,
    private val coverUrl: String,
    private val publishDate: String
) : ExportableItem {

    override fun getExportHeaders(): List<String> = emptyList() // non più usato
    
    override fun toExportRow(): List<String> = listOf(
        title, author, publisher, genre, language, description, pageCount.toString(),
        format, readingStatus, addedDate, rating.toString(), notes, coverUrl, publishDate
    )

    companion object {
        fun headers(context: Context): List<String> = listOf(
            context.getString(R.string.title),
            context.getString(R.string.author),
            context.getString(R.string.book_publisher),
            context.getString(R.string.genre),
            context.getString(R.string.book_language),
            context.getString(R.string.book_description),
            context.getString(R.string.book_page_count),
            context.getString(R.string.format),
            context.getString(R.string.reading_status),
            context.getString(R.string.book_added_date),
            context.getString(R.string.book_rating),
            context.getString(R.string.book_notes),
            context.getString(R.string.book_cover_url),
            context.getString(R.string.publish_date)
        )
    }
}




