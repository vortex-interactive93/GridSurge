package com.example.gridsurge.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.example.gridsurge.meta.PlayerProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object BillingManager : PurchasesUpdatedListener {

    private const val TAG = "BillingManager"

    // Product SKUs configured in Google Play Console -> In-App Products
    const val SKU_NO_ADS_BUNDLE = "gridsurge_no_ads_bundle"
    const val SKU_STARTER_CRATE = "gridsurge_starter_crate"
    const val SKU_HYPER_VAULT = "gridsurge_hyper_vault"

    private lateinit var billingClient: BillingClient
    private lateinit var profileManager: PlayerProfileManager
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun initialize(context: Context, profileManager: PlayerProfileManager) {
        this.profileManager = profileManager

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing setup finished successfully!")
                    _isConnected.value = true
                    queryInAppProducts()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Google Play Billing setup failed code: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                Log.w(TAG, "Billing service disconnected; retrying connection...")
            }
        })
    }

    /**
     * Queries product details (prices, titles, descriptions) directly from Google Play.
     */
    private fun queryInAppProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_NO_ADS_BUNDLE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_STARTER_CRATE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_HYPER_VAULT)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = productDetailsList.associateBy { it.productId }
                _productDetailsMap.value = map
                Log.d(TAG, "Fetched ${map.size} products from Google Play Console!")
            } else {
                Log.w(TAG, "Failed querying product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launches the native Google Play bottom-sheet purchase flow for a product.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val productDetails = _productDetailsMap.value[productId]
        if (productDetails == null) {
            Log.w(TAG, "ProductDetails for $productId not ready or not found.")
            // Fallback for development/testing if Google Play Billing is not linked yet
            if (productId == SKU_NO_ADS_BUNDLE) {
                profileManager.purchaseNoAdsBundle()
            }
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
        Log.d(TAG, "Launched Google Play Billing Flow for $productId -> responseCode: $responseCode")
    }

    /**
     * Called by Google Play when a purchase flow completes.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled Google Play purchase flow.")
        } else {
            Log.w(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    /**
     * Processes and acknowledges/consumes Google Play purchases.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (productId in purchase.products) {
                when (productId) {
                    SKU_NO_ADS_BUNDLE -> {
                        acknowledgePurchase(purchase) {
                            profileManager.purchaseNoAdsBundle()
                        }
                    }
                    SKU_STARTER_CRATE -> {
                        consumePurchase(purchase) {
                            profileManager.addStarCurrency(500)
                        }
                    }
                    SKU_HYPER_VAULT -> {
                        consumePurchase(purchase) {
                            profileManager.addStarCurrency(1500)
                        }
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, onSuccess: () -> Unit) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged successfully!")
                    scope.launch(Dispatchers.Main) { onSuccess() }
                }
            }
        } else {
            scope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    private fun consumePurchase(purchase: Purchase, onSuccess: () -> Unit) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(params) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase consumed successfully!")
                scope.launch(Dispatchers.Main) { onSuccess() }
            }
        }
    }

    /**
     * Restores existing purchases across reinstalls or device switches.
     */
    fun restorePurchases() {
        if (!_isConnected.value) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    handlePurchase(purchase)
                }
            }
        }
    }
}
