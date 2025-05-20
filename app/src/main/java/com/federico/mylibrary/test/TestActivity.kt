package com.federico.mylibrary.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.federico.mylibrary.book.AddBookScreen
import androidx.navigation.compose.rememberNavController
import com.federico.mylibrary.viewmodel.UserViewModel

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
    fun TestAddBookScreen(userViewModel: UserViewModel = viewModel()) {
        val navController = rememberNavController()

        AddBookScreen(
            navController = navController,
            userViewModel = userViewModel,
            overrideGalleryPicker = { println("✅ Mock galleria") },
            overrideCameraPicker = { println("✅ Mock fotocamera") },
            userIdOverride = "test-user"
        )
    }
}
