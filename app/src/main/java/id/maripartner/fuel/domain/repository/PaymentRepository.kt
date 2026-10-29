package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.PaymentOrder
import id.maripartner.fuel.domain.model.PaymentStatus

interface PaymentRepository {
    suspend fun createCheckoutOrder(planCode: String, provider: String): AppResult<PaymentOrder>
    suspend fun verifyPlayBillingPurchase(
        purchaseToken: String,
        orderId: String,
        productId: String,
        basePlanId: String,
        paymentReference: String? = null
    ): AppResult<Boolean>
    suspend fun getPaymentStatus(paymentReference: String): AppResult<PaymentStatus>
    suspend fun getPaymentHistory(): AppResult<List<PaymentStatus>>
}
