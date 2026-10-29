package id.maripartner.fuel.domain.usecase.vehicle

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class GetVehiclesUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(includeArchived: Boolean = true): Flow<AppResult<List<Vehicle>>> {
        return repository.getVehicles(includeArchived)
    }
}

class SaveVehicleUseCase(
    private val repository: VehicleRepository,
    private val validator: ValidateVehicleInputUseCase = ValidateVehicleInputUseCase()
) {
    suspend operator fun invoke(vehicle: Vehicle): AppResult<Vehicle> {
        val validation = validator(vehicle)
        if (!validation.isSuccess) {
            return AppResult.Error((validation as AppResult.Error).exception)
        }

        return if (vehicle.id.isBlank()) {
            repository.createVehicle(vehicle)
        } else {
            repository.updateVehicle(vehicle)
        }
    }
}

class ArchiveVehicleUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String, archive: Boolean): AppResult<Unit> {
        return repository.archiveVehicle(id, archive)
    }
}

class DeleteVehicleUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return repository.deleteVehicle(id)
    }
}
