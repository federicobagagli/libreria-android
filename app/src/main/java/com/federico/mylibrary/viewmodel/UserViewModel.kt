package com.federico.mylibrary.viewmodel

import android.widget.Toast
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

    val maxItemsNonPremium = 2

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

    // --- Gestione Interstitial Ad ---
    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private var adCounter = 0

    fun loadInterstitialAd(context: android.content.Context) {
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712", // ✅ Test ID
            adRequest,
            object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun maybeShowInterstitial(context: android.content.Context) {
        val activity = context as? android.app.Activity ?: return
        adCounter++
        if (adCounter % 5 == 0 && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback =
                object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd(context)
                    }

                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        interstitialAd = null
                        loadInterstitialAd(context)
                    }
                }
            interstitialAd?.show(activity)
        }
    }

}
