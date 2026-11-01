package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.PaymentRepositoryImpl
import id.maripartner.fuel.domain.repository.PaymentRepository

/**
 * Use case untuk membatalkan transaksi pembayaran sebelum kedaluwarsa.
 */
class CancelPaymentUseCase(
    private val repository: PaymentRepository = PaymentRepositoryImpl()
) {
    suspend operator fun invoke(paymentReference: String): AppResult<Unit> {
        if (paymentReference.isBlank()) {
            return AppResult.Error(id.maripartner.fuel.core.common.result.AppException.Validation("Referensi transaksi tidak valid"))
        }
        return repository.cancelPayment(paymentReference)
    }
}
