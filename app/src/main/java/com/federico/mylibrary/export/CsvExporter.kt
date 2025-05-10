package com.federico.mylibrary.export

object CsvExporter {

    fun generateCsvContent(headers: List<String>, rows: List<List<String>>): String {
        val builder = StringBuilder()
        builder.appendLine(headers.joinToString(","))
        for (row in rows) {
            builder.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return builder.toString()
    }

    private fun escapeCsv(value: String): String {
        // Escape double quotes and wrap in quotes if needed
        val escaped = value.replace("\"", "\"\"")
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
