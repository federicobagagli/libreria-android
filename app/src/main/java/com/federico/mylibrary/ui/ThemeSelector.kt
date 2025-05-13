package com.federico.mylibrary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R
import com.federico.mylibrary.ui.theme.AppThemeStyle

@Composable
fun ThemeSelector(
    selectedTheme: AppThemeStyle,
    onThemeSelected: (AppThemeStyle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(stringResource(R.string.theme_selector_label))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedTheme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppThemeStyle.values().forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}
