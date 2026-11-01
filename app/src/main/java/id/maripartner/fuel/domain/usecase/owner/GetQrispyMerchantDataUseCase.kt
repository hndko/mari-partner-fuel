package id.maripartner.fuel.domain.usecase.owner

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.owner.OwnerRepositoryImpl
import id.maripartner.fuel.domain.model.owner.QrispyMerchantData
import id.maripartner.fuel.domain.repository.owner.OwnerRepository

/**
 * Use case untuk mengambil saldo merchant dan mutasi transaksi resmi gateway QRISpy.
 */
class GetQrispyMerchantDataUseCase(
    private val repository: OwnerRepository = OwnerRepositoryImpl()
) {
    suspend operator fun invoke(): AppResult<QrispyMerchantData> {
        return repository.getQrispyMerchantData()
    }
}
