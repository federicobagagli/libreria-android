package com.federico.mylibrary.export

import android.content.Context
import com.federico.mylibrary.R

class BookExportItem(
    private val title: String,
    private val author: String,
    private val genre: String,
    private val publishDate: String
) : ExportableItem {

    override fun getExportHeaders(): List<String> = emptyList() // non più usato

    override fun toExportRow(): List<String> =
        listOf(title, author, genre, publishDate)

    companion object {
        fun headers(context: Context): List<String> = listOf(
            context.getString(R.string.title),
            context.getString(R.string.author),
            context.getString(R.string.genre),
            context.getString(R.string.publish_date)
        )
    }
}
