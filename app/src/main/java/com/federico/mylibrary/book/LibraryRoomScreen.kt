package com.federico.mylibrary.book

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.util.ConfirmDeleteAllDialog

@Composable
fun LibraryRoomScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.library_room_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { navController.navigate("view_library") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_books), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("add") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_book), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("library_summary") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.summary), fontSize = 18.sp)
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete_entire_collection), color = MaterialTheme.colorScheme.onError)
        }
    }

    ConfirmDeleteAllDialog(
        show = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        scope = scope,
        context = context,
        collectionName = "books" // cambia in "books", "records", "movies" a seconda del caso
    )
}
