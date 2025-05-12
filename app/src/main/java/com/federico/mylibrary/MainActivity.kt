@file:OptIn(ExperimentalMaterial3Api::class)

package com.federico.mylibrary


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.federico.mylibrary.backup.BackupScreen
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.export.ExportViewScreen
import com.federico.mylibrary.viewmodel.LibraryFilterViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val auth = FirebaseAuth.getInstance()
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener {
                        currentUser = it.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                if (currentUser != null) {
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
    val exportViewModel: ExportViewModel = viewModel()
    val libraryFilterViewModel: LibraryFilterViewModel = viewModel()

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
            composable("edit_book/{bookId}") { backStackEntry ->
                EditBookScreen(navController, backStackEntry)
            }
            composable("living_room") { LivingRoomScreen(navController) }
            composable("view_library") { ViewLibraryScreen(navController, libraryFilterViewModel) }
            composable("add") { AddBookScreen() }
            composable("backup") { BackupScreen(navController = navController) }
            composable("settings") { SettingsScreen(navController = navController) }
            composable("exportView") {
                ExportViewScreen(navController = navController, exportViewModel = exportViewModel)
            }
            composable("books") {
                BooksScreen(
                    navController = navController,
                    exportViewModel = exportViewModel,
                    filterViewModel = libraryFilterViewModel
                )
            }
            composable("details_book/{bookId}") { backStackEntry ->
                DetailsBookScreen(navController, backStackEntry)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentDestination: androidx.navigation.NavDestination?) {
    NavigationBar {
        val items = listOf(
            NavItem("living_room", Icons.Default.Home, stringResource(R.string.salotto_title)),
            NavItem("settings", Icons.Default.Settings, stringResource(R.string.screen_settings)),
            NavItem("back", Icons.Default.ArrowBack, stringResource(R.string.back))
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    if (item.route == "back") {
                        Log.d("BottomNav", "Back button clicked")
                        val popped = navController.popBackStack()
                        if (!popped) {
                            Log.d("BottomNav", "Nothing to pop, navigating to living_room")
                            navController.navigate("living_room") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Log.d("BottomNav", "Navigated back successfully")
                        }
                    } else {
                        navController.navigate(item.route)
                    }
                }
            )
        }
    }
}



data class NavItem(val route: String, val icon: ImageVector, val label: String)