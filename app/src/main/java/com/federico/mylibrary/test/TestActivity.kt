package com.federico.mylibrary.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.federico.mylibrary.book.AddBookScreen
import androidx.navigation.compose.rememberNavController

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TEST_ACTIVITY", "✅ onCreate chiamato")

        setContent {
            Log.d("TEST_ACTIVITY", "✅ setContent chiamato")
            TestAddBookScreen()
        }
    }

    @Composable
    fun TestAddBookScreen() {
        val navController = rememberNavController()

        AddBookScreen(
            navController = navController,
            overrideGalleryPicker = { println("✅ Mock galleria") },
            overrideCameraPicker = { println("✅ Mock fotocamera") },
            userIdOverride = "test-user"
        )
    }
}
