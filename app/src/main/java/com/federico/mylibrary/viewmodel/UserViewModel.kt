package com.federico.mylibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded

    private val _isDeveloper = MutableStateFlow(false)
    val isDeveloper: StateFlow<Boolean> = _isDeveloper

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _isPremium.value = false
                        _isDeveloper.value = false
                        _isLoaded.value = true
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _isPremium.value = snapshot.getBoolean("isPremium") == true
                        _isDeveloper.value = snapshot.getBoolean("isDeveloper") == true
                        _isLoaded.value = true
                    } else {
                        _isPremium.value = false
                        _isDeveloper.value = false
                        _isLoaded.value = true
                    }
                }
        } else {
            _isLoaded.value = true
        }
    }
}
