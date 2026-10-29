package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.PaymentRepositoryImpl
import id.maripartner.fuel.domain.repository.PaymentRepository

class VerifyPlayBillingPurchaseUseCase(
    private val repository: PaymentRepository = PaymentRepositoryImpl()
) {
    suspend operator fun invoke(
        purchaseToken: String,
        orderId: String,
        productId: String,
        basePlanId: String,
        paymentReference: String? = null
    ): AppResult<Boolean> {
        return repository.verifyPlayBillingPurchase(
            purchaseToken = purchaseToken,
            orderId = orderId,
            productId = productId,
            basePlanId = basePlanId,
            paymentReference = paymentReference
        )
    }
}
