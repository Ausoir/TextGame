package aoa

import android.util.Log
import com.android.billingclient.api.*

object Billing: PurchasesUpdatedListener {
    private lateinit var context: App
    private lateinit var billingClient: BillingClient
    private lateinit var billingClientStateListener: BillingClientStateListener
    private val skuList = listOf("purchase_gem", "purchase_gem_3", "purchase_gem_7")
    var requestPurchase1: () -> Unit = {
        Log.d("billing", "Reestablishing connection")
        billingClient.startConnection(billingClientStateListener)
    }
    var requestPurchase3 = requestPurchase1
    var requestPurchase7 = requestPurchase1

    fun setupBillingClient(context: App) {
        this.context = context
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d("billing", "Setup finished with code ${billingResult.responseCode}")
                Billing.context.onPurchase(-20000 + billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadAllSKUs()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("billing", "Setup disconnected")
            }
        }
        billingClient.startConnection(billingClientStateListener)
    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {
                    when (skuDetails.sku) {
                        "purchase_gem" -> {
                            requestPurchase1 = {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(context, billingFlowParams)
                            }
                        }
                        "purchase_gem_3" -> {
                            requestPurchase3 = {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(context, billingFlowParams)
                            }
                        }
                        "purchase_gem_7" -> {
                            requestPurchase7 = {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(context, billingFlowParams)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Log.d("billing", "Setup disconnected")
        null
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                when (purchase.sku) {
                    "purchase_gem" -> {
                        val consumeParams: ConsumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.consumeAsync(consumeParams) { _, _ -> context.onPurchase(1) }
                    }
                    "purchase_gem_3" -> {
                        val consumeParams: ConsumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.consumeAsync(consumeParams) { _, _ -> context.onPurchase(3) }
                    }
                    "purchase_gem_7" -> {
                        val consumeParams: ConsumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.consumeAsync(consumeParams) { _, _ -> context.onPurchase(7) }
                    }
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            context.onPurchase(0)
        } else {
            context.onPurchase(billingResult.responseCode - 900000)
        }
    }
}