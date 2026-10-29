package id.maripartner.fuel.domain.usecase.vehicle

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.Vehicle
import java.time.Year

/**
 * Validasi input data kendaraan sesuai aturan domain dan batasan bisnis.
 */
class ValidateVehicleInputUseCase {

    operator fun invoke(vehicle: Vehicle): AppResult<Unit> {
        if (vehicle.name.trim().isBlank()) {
            return AppResult.Error(AppException.Validation("Nama kendaraan wajib diisi."))
        }

        if (vehicle.productionYear != null) {
            val maxYear = Year.now().value + 1
            if (vehicle.productionYear < 1900 || vehicle.productionYear > maxYear) {
                return AppResult.Error(AppException.Validation("Tahun kendaraan harus masuk akal (antara 1900 dan $maxYear)."))
            }
        }

        if (vehicle.tankCapacityLiters != null && vehicle.tankCapacityLiters <= 0) {
            return AppResult.Error(AppException.Validation("Kapasitas tangki harus lebih besar dari 0 liter."))
        }

        if (vehicle.lastOdometerKm != null && vehicle.lastOdometerKm < 0) {
            return AppResult.Error(AppException.Validation("Angka odometer tidak boleh negatif."))
        }

        return AppResult.Success(Unit)
    }
}
