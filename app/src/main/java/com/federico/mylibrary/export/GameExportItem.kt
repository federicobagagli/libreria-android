package com.federico.mylibrary.export

import android.content.Context
import com.federico.mylibrary.R
import com.federico.mylibrary.model.Game

class GameExportItem(private val game: Game) : ExportableItem {

    override fun getExportHeaders(): List<String> = emptyList() // non usato

    override fun toExportRow(): List<String> = listOf(
        game.title,
        game.type,
        game.platform,
        game.publisher,
        game.releaseDate,
        game.genre,
        game.language,
        game.description,
        game.minPlayers.toString(),
        game.maxPlayers.toString(),
        game.durationMinutes.toString(),
        game.rating,
        game.notes,
        game.location,
        game.addedDate,
        game.coverUrl
    )

    companion object {
        fun headers(context: Context): List<String> = listOf(
            context.getString(R.string.game_title),
            context.getString(R.string.game_type),
            context.getString(R.string.game_platform),
            context.getString(R.string.game_publisher),
            context.getString(R.string.game_release_date),
            context.getString(R.string.game_genre),
            context.getString(R.string.game_language),
            context.getString(R.string.game_description),
            context.getString(R.string.game_min_players),
            context.getString(R.string.game_max_players),
            context.getString(R.string.game_duration_minutes),
            context.getString(R.string.game_rating),
            context.getString(R.string.game_notes),
            context.getString(R.string.game_location),
            context.getString(R.string.game_added_date),
            context.getString(R.string.game_cover_url)
        )
    }
}
