@file:OptIn(ExperimentalMaterial3Api::class)

package com.federico.mylibrary

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser

                if (user != null) {
                    LibreriaApp()
                } else {
                    LoginScreen(auth)
                }
            }
        }
    }
}

@Composable
fun LibreriaApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination?.route) {
                            "living_room" -> stringResource(R.string.salotto_title)
                            "view_library" -> stringResource(R.string.view_library_title)
                            "books" -> stringResource(R.string.screen_books)
                            "add" -> stringResource(R.string.screen_add)
                            "settings" -> stringResource(R.string.screen_settings)
                            else -> stringResource(R.string.app_name)
                        }
                    )
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, currentDestination)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "living_room",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("living_room") { LivingRoomScreen(navController) }
            composable("view_library") { ViewLibraryScreen(navController) }
            composable("books") { BooksScreen() }
            composable("add") { AddBookScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentDestination: androidx.navigation.NavDestination?) {
    NavigationBar {
        val items = listOf(
            NavItem("living_room", Icons.Default.Home, stringResource(R.string.salotto_title)),
            NavItem("books", Icons.Default.Book, stringResource(R.string.screen_books)),
            NavItem("add", Icons.Default.Add, stringResource(R.string.screen_add)),
            NavItem("settings", Icons.Default.Settings, stringResource(R.string.screen_settings))
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)