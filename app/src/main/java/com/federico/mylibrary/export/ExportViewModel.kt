package com.federico.mylibrary.export


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExportViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ExportableItem>>(emptyList())
    val items: StateFlow<List<ExportableItem>> = _items

    private val _fileName = MutableStateFlow("export.csv")
    val fileName: StateFlow<String> = _fileName

    fun setExportData(items: List<ExportableItem>, fileName: String) {
        _items.value = items
        _fileName.value = fileName
    }
}
