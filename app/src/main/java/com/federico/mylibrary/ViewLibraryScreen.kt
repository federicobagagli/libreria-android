package com.federico.mylibrary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun ViewLibraryScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.view_library_title),
            style = MaterialTheme.typography.titleLarge
        )

        Button(onClick = { navController.navigate("books") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.view_all_books))
        }

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }

        // Potrai aggiungere qui altri filtri/opzioni per la library
    }
}
