package id.maripartner.fuel.domain.usecase.transaction

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelTransaction

/**
 * Validasi input transaksi pengeluaran BBM manual.
 */
class ValidateTransactionInputUseCase {

    operator fun invoke(transaction: FuelTransaction): AppResult<Unit> {
        if (transaction.vehicleId.trim().isBlank()) {
            return AppResult.Error(AppException.Validation("Kendaraan wajib dipilih."))
        }

        if (transaction.totalAmountIdr <= 0) {
            return AppResult.Error(AppException.Validation("Total pembayaran harus lebih besar dari Rp 0."))
        }

        if (transaction.volumeLiters != null && transaction.volumeLiters <= 0) {
            return AppResult.Error(AppException.Validation("Volume BBM harus lebih besar dari 0 liter."))
        }

        if (transaction.pricePerLiterIdr != null && transaction.pricePerLiterIdr <= 0) {
            return AppResult.Error(AppException.Validation("Harga per liter harus lebih besar dari Rp 0."))
        }

        if (transaction.odometerKm != null && transaction.odometerKm < 0) {
            return AppResult.Error(AppException.Validation("Angka odometer tidak boleh bernilai negatif."))
        }

        return AppResult.Success(Unit)
    }
}
