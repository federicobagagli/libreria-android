package com.federico.mylibrary.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.federico.mylibrary.R
import com.federico.mylibrary.billing.BillingManager

@Composable
fun GoPremiumScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val billingManager = remember {
        BillingManager(
            context = context,
            onPremiumPurchased = {
                Toast.makeText(context, context.getString(R.string.upgrade_success), Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        )
    }

    LaunchedEffect(Unit) {
        billingManager.startConnection(scope)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.premium_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.premium_description),
            fontSize = 16.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✔ " + stringResource(R.string.premium_no_limits))
            Text("✔ " + stringResource(R.string.premium_backup))
            Text("✔ " + stringResource(R.string.premium_theme))
            Text("✔ " + stringResource(R.string.premium_share))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            activity?.let {
                billingManager.launchPurchaseFlow(it)
            }
        }) {
            Text(stringResource(R.string.go_premium))
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.cancel))
        }
    }
}
