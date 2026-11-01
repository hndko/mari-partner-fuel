package id.maripartner.fuel.data.remote.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams

class PlayBillingManager(
    context: Context,
    private val onPurchaseSuccess: (Purchase) -> Unit,
    private val onBillingError: (String) -> Unit
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var isConnected = false
    private var pendingOnConnected: (() -> Unit)? = null

    fun startConnection(onConnected: () -> Unit = {}) {
        pendingOnConnected = onConnected
        if (billingClient.isReady) {
            isConnected = true
            onConnected()
        } else {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            isConnected = true
            pendingOnConnected?.invoke()
            pendingOnConnected = null
        } else {
            isConnected = false
            onBillingError("Koneksi Google Play Billing gagal: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        isConnected = false
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        onPurchaseSuccess(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onBillingError("Pembelian dibatalkan oleh pengguna.")
            }
            else -> {
                onBillingError("Terjadi kendala saat pembayaran: ${billingResult.debugMessage}")
            }
        }
    }

    fun querySubscriptionProducts(
        productId: String = "mari_pro",
        onResult: (List<ProductDetails>) -> Unit
    ) {
        val queryProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(queryProductList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(productDetailsList)
            } else {
                onResult(emptyList())
            }
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): Boolean {
        if (!billingClient.isReady) {
            onBillingError("Google Play Billing belum siap. Silakan coba sesaat lagi.")
            return false
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val response = billingClient.launchBillingFlow(activity, billingFlowParams)
        return response.responseCode == BillingClient.BillingResponseCode.OK
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
