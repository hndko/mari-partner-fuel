package id.maripartner.fuel.domain.usecase.vehicle

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import id.maripartner.fuel.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeVehicleRepository : VehicleRepository {
    val vehicles = mutableListOf<Vehicle>()

    override fun getVehicles(includeArchived: Boolean): Flow<AppResult<List<Vehicle>>> {
        val list = if (includeArchived) vehicles else vehicles.filter { !it.isArchived }
        return flowOf(AppResult.Success(list))
    }

    override suspend fun getVehicleById(id: String): AppResult<Vehicle> {
        val v = vehicles.firstOrNull { it.id == id }
        return if (v != null) AppResult.Success(v) else AppResult.Error(AppException.NotFound())
    }

    override suspend fun createVehicle(vehicle: Vehicle): AppResult<Vehicle> {
        val created = vehicle.copy(id = "veh-${vehicles.size + 1}")
        vehicles.add(created)
        return AppResult.Success(created)
    }

    override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> {
        val idx = vehicles.indexOfFirst { it.id == vehicle.id }
        return if (idx >= 0) {
            vehicles[idx] = vehicle
            AppResult.Success(vehicle)
        } else {
            AppResult.Error(AppException.NotFound())
        }
    }

    override suspend fun archiveVehicle(id: String, archive: Boolean): AppResult<Unit> {
        val idx = vehicles.indexOfFirst { it.id == id }
        return if (idx >= 0) {
            vehicles[idx] = vehicles[idx].copy(isArchived = archive)
            AppResult.Success(Unit)
        } else {
            AppResult.Error(AppException.NotFound())
        }
    }

    override suspend fun deleteVehicle(id: String): AppResult<Unit> {
        val removed = vehicles.removeIf { it.id == id }
        return if (removed) AppResult.Success(Unit) else AppResult.Error(AppException.NotFound())
    }
}

class VehicleUseCaseTest {

    private val fakeRepo = FakeVehicleRepository()
    private val getVehiclesUseCase = GetVehiclesUseCase(fakeRepo)
    private val saveVehicleUseCase = SaveVehicleUseCase(fakeRepo)
    private val archiveVehicleUseCase = ArchiveVehicleUseCase(fakeRepo)
    private val deleteVehicleUseCase = DeleteVehicleUseCase(fakeRepo)

    @Test
    fun saveVehicle_newVehicle_createsAndAssignsId() = runTest {
        val newVehicle = Vehicle(
            name = "Vario 160",
            vehicleType = VehicleType.MOTORCYCLE,
            licensePlate = "B 1234 ABC"
        )
        val result = saveVehicleUseCase(newVehicle)
        assertTrue(result.isSuccess)
        val created = (result as AppResult.Success).data
        assertEquals("veh-1", created.id)
        assertEquals(1, fakeRepo.vehicles.size)
    }

    @Test
    fun saveVehicle_invalidInput_failsWithoutSaving() = runTest {
        val invalid = Vehicle(name = "")
        val result = saveVehicleUseCase(invalid)
        assertFalse(result.isSuccess)
        assertEquals(0, fakeRepo.vehicles.size)
    }

    @Test
    fun archiveAndUnarchiveVehicle_togglesStateCorrectly() = runTest {
        val v = Vehicle(id = "v-1", name = "Avanza", isArchived = false)
        fakeRepo.vehicles.add(v)

        val archiveResult = archiveVehicleUseCase("v-1", archive = true)
        assertTrue(archiveResult.isSuccess)
        assertTrue(fakeRepo.vehicles.first().isArchived)

        // GetVehicles with includeArchived = false should be empty
        val activeVehicles = getVehiclesUseCase(includeArchived = false).first()
        assertEquals(0, (activeVehicles as AppResult.Success).data.size)

        // Unarchive
        archiveVehicleUseCase("v-1", archive = false)
        assertFalse(fakeRepo.vehicles.first().isArchived)
    }

    @Test
    fun deleteVehicle_removesFromRepository() = runTest {
        val v = Vehicle(id = "v-1", name = "Supra X")
        fakeRepo.vehicles.add(v)

        val result = deleteVehicleUseCase("v-1")
        assertTrue(result.isSuccess)
        assertEquals(0, fakeRepo.vehicles.size)
    }
}
