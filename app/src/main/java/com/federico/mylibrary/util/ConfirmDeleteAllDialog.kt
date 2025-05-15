package com.federico.mylibrary.util

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.federico.mylibrary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ConfirmDeleteAllDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    scope: CoroutineScope,
    context: Context,
    collectionName: String
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_deletion_title)) },
        text = { Text(stringResource(R.string.confirm_delete_all_message)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                scope.launch {
                    deleteAllFromCollection(collectionName)
                    Toast.makeText(context, context.getString(R.string.deletion_complete), Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private suspend fun deleteAllFromCollection(collectionName: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val docs = db.collection(collectionName)
        .whereEqualTo("userId", userId)
        .get()
        .await()

    for (doc in docs.documents) {
        doc.reference.delete().await()
    }
}
