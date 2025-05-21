package com.federico.mylibrary.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val onPremiumPurchased: () -> Unit
) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    fun startConnection(scope: CoroutineScope) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryExistingPurchases()
                    }
                }
            }
        })
    }

    suspend fun queryExistingPurchases() {
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        result.purchasesList?.forEach { purchase ->
            if (purchase.products.contains("premium_upgrade") &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                updateUserAsPremium()
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("premium_upgrade")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val product = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product)
                                .build()
                        )
                    ).build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.products.contains("premium_upgrade")) {
                    updateUserAsPremium()
                }
            }
        }
    }

    private fun updateUserAsPremium() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("isPremium", true)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance()
                    .collection("logs")
                    .add(mapOf("uid" to uid, "event" to "premium_activated", "timestamp" to System.currentTimeMillis()))

                onPremiumPurchased()
            }
    }
}
