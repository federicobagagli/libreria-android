package com.federico.mylibrary.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.federico.mylibrary.ads.AdBannerView
import com.federico.mylibrary.util.ConfirmDeleteAllDialog
import com.federico.mylibrary.viewmodel.UserViewModel

@Composable
fun GameRoomScreen(navController: NavController, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isPremium by userViewModel.isPremium.collectAsState()
    if (!isPremium) {
        AdBannerView(modifier = Modifier.fillMaxWidth())
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.playroom),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (!isPremium) {
                    userViewModel.maybeShowInterstitial(context)
                }
                navController.navigate("games")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.view_games), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("add_game") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_game), fontSize = 18.sp)
        }

        Button(
            onClick = { navController.navigate("game_summary") },
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
            Text(
                stringResource(R.string.delete_entire_collection),
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
    ConfirmDeleteAllDialog(
        show = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        scope = scope,
        context = context,
        collectionName = "games" // cambia in "books", "records", "movies" a seconda del caso
    )
}
