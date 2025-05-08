@file:OptIn(ExperimentalMaterial3Api::class)

// MainActivity.kt con Login + Navigazione + BottomNavigationBar
package com.federico.mylibrary

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login effettuato!", Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.recreate()
                        } else {
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { registerTask ->
                                    if (registerTask.isSuccessful) {
                                        Toast.makeText(context, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                                        (context as? Activity)?.recreate()
                                    } else {
                                        Toast.makeText(context, "Errore: ${registerTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login / Registrati")
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
                            "books" -> "Libreria"
                            "add" -> "Aggiungi Libro"
                            "settings" -> "Configurazione"
                            else -> "Libreria"
                        }
                    )
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentDestination)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "books",
            modifier = Modifier.padding(innerPadding)
        ) {
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
            NavItem("books", Icons.Default.Book, "Libreria"),
            NavItem("add", Icons.Default.Add, "Aggiungi"),
            NavItem("settings", Icons.Default.Settings, "Config")
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

data class Book(
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val publishDate: String = ""
)

@Composable
fun BooksScreen() {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("books")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            books = snapshot.documents.mapNotNull { it.toObject<Book>() }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (books.isEmpty()) {
            Text("Nessun libro trovato.")
        } else {
            books.forEach { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📖 ${book.title}", style = MaterialTheme.typography.titleMedium)
                        Text("✍️ ${book.author}")
                        Text("📚 ${book.genre}")
                        Text("📅 ${book.publishDate}")
                    }
                }
            }
        }
    }
}

@Composable
fun AddBookScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var publishDate by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autore") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genere") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = publishDate, onValueChange = { publishDate = it }, label = { Text("Data pubblicazione (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            val book = hashMapOf(
                "title" to title,
                "author" to author,
                "genre" to genre,
                "publishDate" to publishDate,
                "userId" to userId
            )
            db.collection("books")
                .add(book)
                .addOnSuccessListener {
                    Toast.makeText(context, "Libro aggiunto!", Toast.LENGTH_SHORT).show()
                    title = ""; author = ""; genre = ""; publishDate = ""
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Salva libro")
        }
    }
}

@Composable
fun SettingsScreen() {
    Text("⚙️ Configurazione")
}
