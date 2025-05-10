package com.federico.mylibrary.export

interface ExportableItem {
    fun getExportHeaders(): List<String>
    fun toExportRow(): List<String>
}
