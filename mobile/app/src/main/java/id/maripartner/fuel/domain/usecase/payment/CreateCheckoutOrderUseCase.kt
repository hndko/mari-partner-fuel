package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.PaymentRepositoryImpl
import id.maripartner.fuel.domain.model.PaymentOrder
import id.maripartner.fuel.domain.repository.PaymentRepository

class CreateCheckoutOrderUseCase(
    private val repository: PaymentRepository = PaymentRepositoryImpl()
) {
    suspend operator fun invoke(planCode: String, provider: String): AppResult<PaymentOrder> {
        return repository.createCheckoutOrder(planCode = planCode, provider = provider)
    }
}
