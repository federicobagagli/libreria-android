package com.federico.mylibrary

import android.app.Activity
import android.content.IntentSender
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }

    val oneTapClient = remember { Identity.getSignInClient(context) }

    val googleLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener {
                            Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            activity.recreate()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, context.getString(R.string.login_failed, it.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.login_failed, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        // 1. Pulsante Google
        Button(
            onClick = {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId("1048852056688-q09i8vblaf6fm9sdopok83vusv3vpino.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            val intentSender = result.pendingIntent.intentSender
                            googleLoginLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Toast.makeText(context, context.getString(R.string.google_intent_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, context.getString(R.string.google_login_failed, it.message ?: ""), Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_with_google))
        }

        Spacer(modifier = Modifier.height(16.dp))

// 2. Frase "oppure accedi con"
        Text(
            text = stringResource(R.string.or_login_with),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

// 3. Campi email + password
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

        Spacer(modifier = Modifier.height(16.dp))

// 4. Bottoni login/registrazione email
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            activity.recreate()
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
                            activity.recreate()
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
