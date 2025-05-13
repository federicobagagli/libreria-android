package com.federico.mylibrary.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecordFilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(RecordFilterState())
    val filterState: StateFlow<RecordFilterState> = _filterState

    fun updateFilters(newFilters: RecordFilterState) {
        _filterState.value = newFilters
    }

    fun clearFilters() {
        _filterState.value = RecordFilterState()
    }
}
