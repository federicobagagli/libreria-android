@file:OptIn(ExperimentalMaterial3Api::class)

package com.federico.mylibrary


import android.content.Intent
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
import com.federico.mylibrary.debug.CrashScreen
import com.federico.mylibrary.export.ExportViewModel
import com.federico.mylibrary.export.ExportViewScreen
import com.federico.mylibrary.game.AddGameScreen
import com.federico.mylibrary.game.DetailsGameScreen
import com.federico.mylibrary.game.EditGameScreen
import com.federico.mylibrary.game.GameAdvancedSummaryScreen
import com.federico.mylibrary.game.GameBarChartsScreen
import com.federico.mylibrary.game.GamePieChartsScreen
import com.federico.mylibrary.game.GameRoomScreen
import com.federico.mylibrary.game.GameSummaryScreen
import com.federico.mylibrary.game.GamesScreen
import com.federico.mylibrary.game.ViewGamesScreen
import com.federico.mylibrary.movie.AddMovieScreen
import com.federico.mylibrary.movie.DetailsMovieScreen
import com.federico.mylibrary.movie.EditMovieScreen
import com.federico.mylibrary.movie.MovieAdvancedSummaryScreen
import com.federico.mylibrary.movie.MovieBarChartsScreen
import com.federico.mylibrary.movie.MoviePieChartsScreen
import com.federico.mylibrary.movie.MovieRoomScreen
import com.federico.mylibrary.movie.MovieSummaryScreen
import com.federico.mylibrary.movie.MoviesScreen
import com.federico.mylibrary.movie.ViewMoviesScreen
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
import com.federico.mylibrary.record.RecordSummaryScreen
import com.federico.mylibrary.record.RecordAdvancedSummaryScreen
import com.federico.mylibrary.record.RecordBarChartsScreen
import com.federico.mylibrary.record.RecordPieChartsScreen
import com.federico.mylibrary.viewmodel.GameFilterViewModel
import com.federico.mylibrary.viewmodel.MovieFilterViewModel
import com.federico.mylibrary.viewmodel.UserViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.federico.mylibrary.book.IsbnScannerScreen
import com.federico.mylibrary.ui.GoPremiumScreen
import com.federico.mylibrary.util.Logger
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        val testDeviceIds = listOf("78B6EDB6E9C9C53A0E77B00DAE9BFCF8")
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        val currentUser = FirebaseAuth.getInstance().currentUser
        Logger.d("FIREBASE_UID", "UID: ${currentUser?.uid}")
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
    val movieFilterViewModel: MovieFilterViewModel = viewModel()
    val gameFilterViewModel: GameFilterViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    LaunchedEffect(userViewModel) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userViewModel.startUserListener(it) }
    }
    val hideTopBarRoutes = listOf("books", "view_library", "records", "view_records")
    Scaffold(
        topBar = {
            if (currentDestination?.route !in hideTopBarRoutes) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentDestination?.route) {
                                "living_room" -> stringResource(R.string.salotto_title)
                                else -> ""
                            }
                        )
                    }
                )
            }
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

            composable("about") {
                AboutScreen()
            }
            composable("edit_book/{bookId}") { backStackEntry ->
                EditBookScreen(navController, backStackEntry)
            }
            composable("living_room") { LivingRoomScreen(navController,
                userViewModel = userViewModel) }
            composable("view_library") { ViewLibraryScreen(navController, libraryFilterViewModel) }
            composable("add") { AddBookScreen(navController,userViewModel = userViewModel) }
            composable("backup") { BackupScreen(navController = navController) }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    userViewModel = userViewModel
                )
            }
            composable("library_room") {
                LibraryRoomScreen(navController,
                                    userViewModel = userViewModel) }
            composable("library_summary") { LibrarySummaryScreen(navController) }
            composable("exportView") { ExportViewScreen(navController = navController, exportViewModel = exportViewModel) }
            composable("library_advanced_summary") { LibraryAdvancedSummaryScreen(navController) }
            composable("library_pie_charts") { LibraryPieChartsScreen(navController) }
            composable("library_bar_charts") { LibraryBarChartsScreen(navController) }
            composable("books") {
                BooksScreen(
                    navController = navController,
                    exportViewModel = exportViewModel,
                    filterViewModel = libraryFilterViewModel,
                    userViewModel = userViewModel
                )
            }
            composable("details_book/{bookId}") { backStackEntry ->
                DetailsBookScreen(navController, backStackEntry)
            }

            //Records
            composable("edit_record/{recordId}") { backStackEntry ->
                EditRecordScreen(navController, backStackEntry)
            }
            composable("record_room") { RecordRoomScreen(navController,
                userViewModel = userViewModel) }
            composable("add_record") { AddRecordScreen(navController,userViewModel = userViewModel) }
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
            composable("record_summary") { RecordSummaryScreen(navController) }
            composable("record_advanced_summary") { RecordAdvancedSummaryScreen(navController) }
            composable("record_pie_charts") { RecordPieChartsScreen(navController) }
            composable("record_bar_charts") { RecordBarChartsScreen(navController) }

            //Movies
            composable("movie_room") { MovieRoomScreen(navController,
                userViewModel = userViewModel) }
            composable("add_movie") { AddMovieScreen(navController,userViewModel = userViewModel) }
            composable("view_movies") { ViewMoviesScreen(navController, movieFilterViewModel) }
            composable("movies") {
                MoviesScreen(
                    navController = navController,
                    exportViewModel = exportViewModel,
                    filterViewModel = movieFilterViewModel
                )
            }
            composable("details_movie/{movieId}") { backStackEntry ->
                DetailsMovieScreen(navController, backStackEntry)
            }
            composable("edit_movie/{movieId}") { backStackEntry ->
                EditMovieScreen(navController, backStackEntry)
            }
            composable("movie_summary") { MovieSummaryScreen(navController) }
            composable("movie_advanced_summary") { MovieAdvancedSummaryScreen(navController) }
            composable("movie_pie_charts") { MoviePieChartsScreen(navController) }
            composable("movie_bar_charts") { MovieBarChartsScreen(navController) }

            //Games
            composable("game_room") { GameRoomScreen(navController,
                userViewModel = userViewModel) }
            composable("add_game") { AddGameScreen(navController,userViewModel = userViewModel) }
            composable("view_games") { ViewGamesScreen(navController, gameFilterViewModel) }
            composable("games") {
                GamesScreen(
                    navController = navController,
                    exportViewModel = exportViewModel,
                    filterViewModel = gameFilterViewModel
                )
            }
            composable("details_game/{gameId}") { backStackEntry ->
                DetailsGameScreen(navController, backStackEntry)
            }
            composable("edit_game/{gameId}") { backStackEntry ->
                EditGameScreen(navController, backStackEntry)
            }
            composable("game_summary") { GameSummaryScreen(navController) }
            composable("game_advanced_summary") { GameAdvancedSummaryScreen(navController) }
            composable("game_pie_charts") { GamePieChartsScreen(navController) }
            composable("game_bar_charts") { GameBarChartsScreen(navController) }

            composable("scan_isbn_live") {
                IsbnScannerScreen(
                    navController = navController,
                    onIsbnDetected = { isbn ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scannedIsbn", isbn)
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
            composable("go_premium") {
                GoPremiumScreen(navController)
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
                        Logger.d("BottomNav", "Back button clicked")
                        val popped = navController.popBackStack()
                        if (!popped) {
                            Logger.d("BottomNav", "Nothing to pop, navigating to living_room")
                            navController.navigate("living_room") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Logger.d("BottomNav", "Navigated back successfully")
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