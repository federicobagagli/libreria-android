package com.federico.mylibrary

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }

    if (showReset) {
        PasswordResetScreen(onBack = { showReset = false })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.recreate()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.login_failed, task.exception?.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_button))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { registerTask ->
                        if (registerTask.isSuccessful) {
                            Toast.makeText(context, context.getString(R.string.registration_success), Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.recreate()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.registration_failed, registerTask.exception?.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_button))
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { showReset = true }, modifier = Modifier.align(Alignment.End)) {
            Text(text = stringResource(R.string.password_reset_title))
        }
    }
}
