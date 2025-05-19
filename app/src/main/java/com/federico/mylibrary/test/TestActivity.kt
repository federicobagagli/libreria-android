package com.federico.mylibrary.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.federico.mylibrary.book.AddBookScreen

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TEST_ACTIVITY", "✅ onCreate chiamato")

        setContent {
            Log.d("TEST_ACTIVITY", "✅ setContent chiamato")
            AddBookScreen(
                overrideGalleryPicker = { println("✅ Mock galleria") },
                overrideCameraPicker = { println("✅ Mock fotocamera") },
                userIdOverride = "test-user"
            )
        }
    }
}
