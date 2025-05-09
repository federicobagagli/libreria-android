package com.federico.mylibrary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun LivingRoomScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Immagine di sfondo
        Image(
            painter = painterResource(id = R.drawable.sfondo_salotto),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay per rendere leggibile il contenuto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
        )

        // Contenuto principale
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { navController.navigate("view_library") }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.library))
            }

            Button(onClick = { /* TODO: navigate to record library */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.record_library))
            }

            Button(onClick = { /* TODO: navigate to movie library */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.movie_library))
            }

            Button(onClick = { /* TODO: navigate to playroom */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.playroom))
            }
        }
    }
}
