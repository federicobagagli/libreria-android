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
import com.federico.mylibrary.backup.BackupScreen
import com.federico.mylibrary.book.AddBookScreen
import com.federico.mylibrary.book.BooksScreen
import com.federico.mylibrary.book.DetailsBookScreen
import com.federico.mylibrary.book.EditBookScreen
import com.federico.mylibrary.book.LibraryAdvancedSummaryScreen
import com.federico.mylibrary.book.LibraryBarChartsScreen
import com.federico.mylibrary.book.LibraryPieChartsScreen
import com.federico.mylibrary.book.LibraryRoomScreen
import com.federico.mylibrary.book.LibrarySummaryScreen
import com.federico.mylibrary.book.ViewLibraryScreen
import com.federico.mylibrary.datastore.ThemePreferences
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.export.ExportViewScreen
import com.federico.mylibrary.record.RecordRoomScreen
import com.federico.mylibrary.ui.theme.AppThemeStyle
import com.federico.mylibrary.ui.theme.LibraryAppTheme
import com.federico.mylibrary.viewmodel.LibraryFilterViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.federico.mylibrary.record.AddRecordScreen
import com.federico.mylibrary.record.DetailsRecordScreen
import com.federico.mylibrary.record.EditRecordScreen
import com.federico.mylibrary.record.RecordsScreen
import com.federico.mylibrary.record.ViewRecordsScreen
import com.federico.mylibrary.viewmodel.RecordFilterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val selectedThemeStyle by ThemePreferences.themeStyleFlow(context).collectAsState(initial = AppThemeStyle.SYSTEM)


            val coroutineScope = rememberCoroutineScope()

            LibraryAppTheme(themeStyle = selectedThemeStyle) {
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
                    LibreriaApp(
                        selectedTheme = selectedThemeStyle,
                        onThemeSelected = { theme ->
                            coroutineScope.launch {
                                ThemePreferences.setThemeStyle(context, theme)
                            }
                        }
                    )
                } else {
                    LoginScreen(auth)
                }
            }
        }
    }
}

@Composable
fun LibreriaApp(selectedTheme: AppThemeStyle,
                onThemeSelected: (AppThemeStyle) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val exportViewModel: ExportViewModel = viewModel()
    val libraryFilterViewModel: LibraryFilterViewModel = viewModel()
    val recordFilterViewModel: RecordFilterViewModel = viewModel()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination?.route) {
                            "living_room" -> stringResource(R.string.salotto_title)
                            "view_library" -> ""//stringResource(R.string.view_library_title)
                            "books" -> ""//stringResource(R.string.screen_books)
                            "add" -> stringResource(R.string.screen_add)
                            "settings" -> stringResource(R.string.screen_settings)
                            else -> ""//stringResource(R.string.app_name)
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
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected
                )
            }
            composable("library_room") { LibraryRoomScreen(navController) }
            composable("library_summary") { LibrarySummaryScreen(navController) }
            composable("exportView") { ExportViewScreen(navController = navController, exportViewModel = exportViewModel) }
            composable("library_advanced_summary") { LibraryAdvancedSummaryScreen(navController) }
            composable("library_pie_charts") { LibraryPieChartsScreen(navController) }
            composable("library_bar_charts") { LibraryBarChartsScreen(navController) }
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

            //Records
            composable("edit_record/{recordId}") { backStackEntry ->
                EditRecordScreen(navController, backStackEntry)
            }
            composable("record_room") { RecordRoomScreen(navController) }
            composable("add_record") { AddRecordScreen(navController) }
            composable("view_records") { ViewRecordsScreen(navController, recordFilterViewModel) }
            composable("records") {
                RecordsScreen(
                    navController = navController,
                    exportViewModel = exportViewModel,
                    filterViewModel = recordFilterViewModel
                )
            }
            composable("details_record/{recordId}") { backStackEntry ->
                DetailsRecordScreen(navController, backStackEntry)
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