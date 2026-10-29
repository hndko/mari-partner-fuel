package id.maripartner.fuel.feature.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.VehicleRepositoryImpl
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.repository.VehicleRepository
import id.maripartner.fuel.domain.usecase.vehicle.ArchiveVehicleUseCase
import id.maripartner.fuel.domain.usecase.vehicle.DeleteVehicleUseCase
import id.maripartner.fuel.domain.usecase.vehicle.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehiclesViewModel(
    private val repository: VehicleRepository = VehicleRepositoryImpl(),
    private val getVehiclesUseCase: GetVehiclesUseCase = GetVehiclesUseCase(repository),
    private val archiveVehicleUseCase: ArchiveVehicleUseCase = ArchiveVehicleUseCase(repository),
    private val deleteVehicleUseCase: DeleteVehicleUseCase = DeleteVehicleUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState(isLoading = true))
    val uiState: StateFlow<VehiclesUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    fun onEvent(event: VehicleUiEvent) {
        when (event) {
            is VehicleUiEvent.SetFilterTab -> {
                _uiState.update { it.copy(filterTab = event.tab) }
            }
            is VehicleUiEvent.ArchiveVehicle -> {
                archiveVehicle(event.id, event.archive)
            }
            is VehicleUiEvent.DeleteVehicle -> {
                deleteVehicle(event.id)
            }
            is VehicleUiEvent.Refresh -> {
                loadVehicles()
            }
            is VehicleUiEvent.DismissMessage -> {
                _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
            }
        }
    }

    private fun loadVehicles() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getVehiclesUseCase(includeArchived = true).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                vehicles = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exception.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun archiveVehicle(id: String, archive: Boolean) {
        viewModelScope.launch {
            val result = archiveVehicleUseCase(id, archive)
            when (result) {
                is AppResult.Success -> {
                    val msg = if (archive) "Kendaraan berhasil diarsipkan" else "Kendaraan berhasil diaktifkan kembali"
                    _uiState.update { it.copy(actionMessage = msg) }
                    loadVehicles()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
            }
        }
    }

    private fun deleteVehicle(id: String) {
        viewModelScope.launch {
            val result = deleteVehicleUseCase(id)
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(actionMessage = "Kendaraan berhasil dihapus") }
                    loadVehicles()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
            }
        }
    }
}
