package com.federico.mylibrary

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.Composable

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


        val loginSuccess = stringResource(R.string.login_success)
        val registrationSucces =  stringResource(R.string.registration_success)
        val errorPrefix = stringResource(R.string.error_prefix)
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, loginSuccess, Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.recreate()
                        } else {
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { registerTask ->
                                    if (registerTask.isSuccessful) {
                                        Toast.makeText(context,registrationSucces, Toast.LENGTH_SHORT).show()
                                        (context as? Activity)?.recreate()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "errorPrefix ${registerTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_or_register))
        }
    }
}
