package com.federico.mylibrary.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GameFilterState(
    val title: String = "",
    val type: String = "",         // board, videogame, altro
    val platform: String = "",
    val publisher: String = "",
    val releaseDate: String = "",
    val genre: String = "",
    val language: String = "",
    val description: String = "",
    val minPlayers: String = "",
    val maxPlayers: String = "",
    val durationMinutes: String = "",
    val rating: String = "",
    val notes: String = "",
    val location: String = "",
    val addedDate: String = "",
    val coverUrl: String = ""
)

class GameFilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(GameFilterState())
    val filterState: StateFlow<GameFilterState> = _filterState

    fun updateFilters(newState: GameFilterState) {
        _filterState.value = newState
    }

    fun clearFilters() {
        _filterState.value = GameFilterState()
    }
}
