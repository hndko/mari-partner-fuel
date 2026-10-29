package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.PaymentRepositoryImpl
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.repository.PaymentRepository

class GetPaymentHistoryUseCase(
    private val repository: PaymentRepository = PaymentRepositoryImpl()
) {
    suspend operator fun invoke(): AppResult<List<PaymentStatus>> {
        return repository.getPaymentHistory()
    }
}
