package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.PaymentRepositoryImpl
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.repository.PaymentRepository

class GetPaymentStatusUseCase(
    private val repository: PaymentRepository = PaymentRepositoryImpl()
) {
    suspend operator fun invoke(paymentReference: String): AppResult<PaymentStatus> {
        return repository.getPaymentStatus(paymentReference)
    }
}
