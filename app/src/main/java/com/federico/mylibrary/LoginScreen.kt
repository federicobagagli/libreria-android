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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore

fun isStrongPassword(password: String): Boolean {
    val lengthOk = password.length >= 8
    val upper = password.any { it.isUpperCase() }
    val lower = password.any { it.isLowerCase() }
    val digit = password.any { it.isDigit() }
    val special = password.any { !it.isLetterOrDigit() }

    return lengthOk && upper && lower && digit && special
}

fun ensureUserDocumentExists(uid: String) {
    val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)
    userDoc.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            userDoc.set(
                mapOf(
                    "isPremium" to false,
                    "isDeveloper" to false
                )
            )
        }
    }
}


@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }
    var showWeakPasswordDialog by remember { mutableStateOf(false) }
    val loginEnabled = email.isNotBlank() && password.isNotBlank()


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
                            val user = auth.currentUser
                            if (user != null) {
                                ensureUserDocumentExists(user.uid)
                            }
                            Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            activity.recreate()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, context.getString(R.string.login_failed, it.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("Login crash")
                FirebaseCrashlytics.getInstance().recordException(e)
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
                    //.setAutoSelectEnabled(true)
                    .setAutoSelectEnabled(false)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            val intentSender = result.pendingIntent.intentSender
                            googleLoginLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            FirebaseCrashlytics.getInstance().log("crash in googleLoginLauncher")
                            FirebaseCrashlytics.getInstance().recordException(e)
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

        Text(
            text = stringResource(R.string.or_login_with),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let { ensureUserDocumentExists(it.uid) }
                            if (user != null && !user.isEmailVerified) {
                                user.sendEmailVerification()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.verify_email_sent),
                                    Toast.LENGTH_LONG
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                return@addOnCompleteListener
                            }
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
            modifier = Modifier.fillMaxWidth(),
            enabled = loginEnabled
        ) {
            Text(stringResource(R.string.login_button))
        }


        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (!isStrongPassword(password)) {
                    showWeakPasswordDialog = true
                    return@Button
                }
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { registerTask ->
                        if (registerTask.isSuccessful) {
                            val user = auth.currentUser
                            user?.let { ensureUserDocumentExists(it.uid) }
                            user?.sendEmailVerification()
                            Toast.makeText(
                                context,
                                context.getString(R.string.verify_email_sent),
                                Toast.LENGTH_LONG
                            ).show()
                            FirebaseAuth.getInstance().signOut()
                        } else {
                            val errorMsg = registerTask.exception?.message.orEmpty()
                            if ("password" in errorMsg.lowercase() && "6" in errorMsg) {
                                showWeakPasswordDialog = true
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.registration_failed, errorMsg),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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



    if (showWeakPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showWeakPasswordDialog = false },
            title = { Text(stringResource(R.string.weak_password_title)) },
            text = { Text(stringResource(R.string.weak_password_message)) },
            confirmButton = {
                TextButton(onClick = { showWeakPasswordDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}