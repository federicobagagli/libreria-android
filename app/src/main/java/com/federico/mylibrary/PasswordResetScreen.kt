package com.federico.mylibrary

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
fun PasswordResetScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.password_reset_title), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.password_reset_email_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            if (email.isNotBlank()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, context.getString(R.string.password_reset_success), Toast.LENGTH_LONG).show()
                            onBack()
                        } else {
                            Toast.makeText(context, context.getString(R.string.password_reset_error, task.exception?.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.password_reset_send_button))
        }

        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.back))
        }
    }
}
