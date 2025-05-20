package com.federico.mylibrary.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.federico.mylibrary.R

@Composable
fun GoPremiumButton(navController: NavController,modifier: Modifier = Modifier) {
    Button(onClick = {
        navController.navigate("go_premium")
    },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer)) {
        Text(stringResource(R.string.go_premium))
    }
}
