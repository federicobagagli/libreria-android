package com.federico.mylibrary

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = stringResource(R.string.app_version, "1.0.0"))
        Text(text = stringResource(R.string.app_description))

        HorizontalDivider()

        Text(text = stringResource(R.string.tmdb_credit), style = MaterialTheme.typography.bodyMedium)
        Image(
            painter = painterResource(R.drawable.tmdb_logo),
            contentDescription = "TMDb Logo",
            modifier = Modifier
                .height(48.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org"))
                    context.startActivity(intent)
                }
        )
    }
}
