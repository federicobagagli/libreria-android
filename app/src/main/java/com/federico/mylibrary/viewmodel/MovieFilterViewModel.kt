package com.federico.mylibrary.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MovieFilterState(
    val title: String = "",
    val director: String = "",
    val productionCompany: String = "",
    val genre: String = "",
    val language: String = "",
    val publishDate: String = "",
    val description: String = "",
    val duration: String = "",
    val format: String = "",
    val rating: String = "",
    val notes: String = "",
    val coverUrl: String = "",
    val location: String = ""
)

class MovieFilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(MovieFilterState())
    val filterState: StateFlow<MovieFilterState> = _filterState

    fun updateFilters(newState: MovieFilterState) {
        _filterState.value = newState
    }

    fun clearFilters() {
        _filterState.value = MovieFilterState()
    }
}
