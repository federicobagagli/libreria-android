package com.federico.mylibrary

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.federico.mylibrary.ui.ThemeSelector
import com.federico.mylibrary.ui.theme.AppThemeStyle
import com.federico.mylibrary.util.deleteUserAndData
import com.federico.mylibrary.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    navController: NavController,
    selectedTheme: AppThemeStyle,
    onThemeSelected: (AppThemeStyle) -> Unit,
    userViewModel: UserViewModel
) {
    val isDeveloper by userViewModel.isDeveloper.collectAsState()
    val isPremium by userViewModel.isPremium.collectAsState()
    val isLoaded by userViewModel.isLoaded.collectAsState()
    var showUpgradeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate("backup") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.backup_section_title))
        }

        if (!isLoaded) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        } else {
            ThemeSelector(
                selectedTheme = selectedTheme,
                onThemeSelected = {
                    if (isPremium) {
                        onThemeSelected(it)
                    } else {
                        showUpgradeDialog = true
                    }
                }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Button(
            onClick = { navController.navigate("about") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.about_title))
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.logout), color = Color.White)
            }

            Button(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.delete_account_title), color = Color.White)
            }
        }

        if (isDeveloper) {
            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(userId)

                    docRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val current = document.getBoolean("isPremium") ?: false
                            val newValue = !current

                            docRef.update("isPremium", newValue)
                                .addOnSuccessListener {
                                    Log.d("TOGGLE_PREMIUM", "✅ isPremium aggiornato a $newValue")
                                    Toast.makeText(context,
                                        "isPremium aggiornato a $newValue",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Log.e("TOGGLE_PREMIUM", "❌ Errore update: ${it.message}")
                                }
                        } else {
                            Log.e("TOGGLE_PREMIUM", "❌ Documento utente non trovato.")
                        }
                    }.addOnFailureListener {
                        Log.e("TOGGLE_PREMIUM", "❌ Errore lettura documento: ${it.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("🔁 Toggle Premium (DEV ONLY)")
            }
        }
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text(stringResource(R.string.premium_required_title)) },
            text = { Text(stringResource(R.string.premium_required_message)) },
            confirmButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.confirm_logout_title)) },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    showLogoutDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.delete_account_confirm_title)) },
            text = { Text(stringResource(R.string.delete_account_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteUserAndData(context) {
                        FirebaseAuth.getInstance().signOut()
                        showDeleteAccountDialog = false
                    }
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
