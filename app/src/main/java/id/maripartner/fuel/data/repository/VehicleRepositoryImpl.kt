package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.VehicleDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.repository.VehicleRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementasi repositori kendaraan menggunakan Supabase Postgrest & Auth.
 */
class VehicleRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val auth: Auth? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : VehicleRepository {

    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest
    private fun getAuth(): Auth = auth ?: SupabaseClientProvider.auth

    private fun getCurrentUserId(): String? {
        return getAuth().currentUserOrNull()?.id
    }

    override fun getVehicles(includeArchived: Boolean): Flow<AppResult<List<Vehicle>>> = flow {
        val userId = getCurrentUserId()
        if (userId == null) {
            emit(AppResult.Error(AppException.Authentication("Sesi kedaluwarsa atau pengguna belum login.")))
            return@flow
        }

        try {
            val response = getPostgrest().from("vehicles").select {
                filter {
                    eq("user_id", userId)
                    if (!includeArchived) {
                        eq("is_archived", false)
                    }
                }
                order("is_archived", Order.ASCENDING)
                order("name", Order.ASCENDING)
            }.decodeList<VehicleDto>()

            val vehicles = response.map { it.toDomain() }
            emit(AppResult.Success(vehicles))
        } catch (e: Exception) {
            emit(AppResult.Error(AppException.Network("Gagal memuat daftar kendaraan: ${e.message}", e)))
        }
    }.flowOn(dispatcher)

    override suspend fun getVehicleById(id: String): AppResult<Vehicle> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = getPostgrest().from("vehicles").select {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<VehicleDto>()

            if (dto != null) {
                AppResult.Success(dto.toDomain())
            } else {
                AppResult.Error(AppException.NotFound("Kendaraan tidak ditemukan atau bukan milik Anda."))
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal mengambil data kendaraan: ${e.message}", e))
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): AppResult<Vehicle> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = VehicleDto.fromDomain(vehicle, userId = userId)
            val created = getPostgrest().from("vehicles").insert(dto) {
                select()
            }.decodeSingle<VehicleDto>()

            AppResult.Success(created.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal menambahkan kendaraan: ${e.message}", e))
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): AppResult<Vehicle> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = VehicleDto.fromDomain(vehicle, userId = userId)
            val updated = getPostgrest().from("vehicles").update(dto) {
                filter {
                    eq("id", vehicle.id)
                    eq("user_id", userId)
                }
                select()
            }.decodeSingle<VehicleDto>()

            AppResult.Success(updated.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memperbarui kendaraan: ${e.message}", e))
        }
    }

    override suspend fun archiveVehicle(id: String, archive: Boolean): AppResult<Unit> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            getPostgrest().from("vehicles").update(
                mapOf("is_archived" to archive)
            ) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal mengubah status arsip kendaraan: ${e.message}", e))
        }
    }

    override suspend fun deleteVehicle(id: String): AppResult<Unit> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            getPostgrest().from("vehicles").delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("violates foreign key", ignoreCase = true) || msg.contains("constraint", ignoreCase = true)) {
                AppResult.Error(AppException.Validation("Kendaraan tidak dapat dihapus karena sudah memiliki riwayat transaksi BBM. Silakan gunakan opsi arsip kendaraan."))
            } else {
                AppResult.Error(AppException.Network("Gagal menghapus kendaraan: ${e.message}", e))
            }
        }
    }
}
