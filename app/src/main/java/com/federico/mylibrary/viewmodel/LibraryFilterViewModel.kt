package com.federico.mylibrary.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Tutti i campi di Book (escluso id e userId) per il filtro
data class BookFilterState(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val genre: String = "",
    val language: String = "",
    val publishDate: String = "",
    val description: String = "",
    val pageCount: String = "",
    val format: String = "",
    val readingStatus: String = "",
    val addedDate: String = "",
    val rating: String = "",
    val notes: String = "",
    val coverUrl: String = "",
    val location: String = ""
)

class LibraryFilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(BookFilterState())
    val filterState: StateFlow<BookFilterState> = _filterState

    fun updateFilters(newState: BookFilterState) {
        _filterState.value = newState
    }

    fun clearFilters() {
        _filterState.value = BookFilterState()
    }
}
