package com.federico.mylibrary.export

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.federico.mylibrary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportViewScreen(
    navController: NavController,
    exportViewModel: ExportViewModel
) {
    val items by exportViewModel.items.collectAsState()
    val fileName by exportViewModel.fileName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ExportView(items = items, fileName = fileName)
        }
    }

    BackHandler {
        navController.popBackStack()
    }
}
