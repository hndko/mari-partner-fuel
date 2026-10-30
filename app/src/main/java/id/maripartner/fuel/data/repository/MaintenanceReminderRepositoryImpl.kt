package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.MaintenanceReminderDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.MaintenanceReminder
import id.maripartner.fuel.domain.repository.MaintenanceReminderRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Implementasi repositori pengingat perawatan menggunakan Supabase Postgrest & Auth.
 */
class MaintenanceReminderRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val auth: Auth? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : MaintenanceReminderRepository {

    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest
    private fun getAuth(): Auth = auth ?: SupabaseClientProvider.auth

    private fun getCurrentUserId(): String? {
        return getAuth().currentUserOrNull()?.id
    }

    private fun sanitizeError(e: Exception, fallbackMsg: String): String {
        val raw = e.message ?: return fallbackMsg
        if (raw.contains("Bearer", ignoreCase = true) || 
            raw.contains("apikey", ignoreCase = true) || 
            raw.contains("http://", ignoreCase = true) || 
            raw.contains("https://", ignoreCase = true)) {
            return fallbackMsg
        }
        return "$fallbackMsg: $raw"
    }

    override suspend fun getReminders(
        vehicleId: String?,
        isCompleted: Boolean?
    ): AppResult<List<MaintenanceReminder>> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val response = getPostgrest().from("maintenance_reminders").select {
                filter {
                    eq("user_id", userId)
                    vehicleId?.takeIf { it.isNotBlank() }?.let { eq("vehicle_id", it) }
                    isCompleted?.let { eq("is_completed", it) }
                }
                order("is_completed", Order.ASCENDING)
                order("target_date", Order.ASCENDING)
            }.decodeList<MaintenanceReminderDto>()

            val domainList = response.map { it.toDomain() }
            AppResult.Success(domainList)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal memuat pengingat perawatan"), e))
        }
    }

    override suspend fun getReminderById(id: String): AppResult<MaintenanceReminder> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = getPostgrest().from("maintenance_reminders").select {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<MaintenanceReminderDto>()

            if (dto != null) {
                AppResult.Success(dto.toDomain())
            } else {
                AppResult.Error(AppException.Validation("Pengingat tidak ditemukan."))
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal memuat detail pengingat"), e))
        }
    }

    override suspend fun createReminder(reminder: MaintenanceReminder): AppResult<MaintenanceReminder> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = MaintenanceReminderDto.fromDomain(reminder, userId)
            val createdDto = getPostgrest().from("maintenance_reminders").insert(dto) {
                select()
            }.decodeSingle<MaintenanceReminderDto>()

            AppResult.Success(createdDto.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal menyimpan pengingat"), e))
        }
    }

    override suspend fun updateReminder(reminder: MaintenanceReminder): AppResult<MaintenanceReminder> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = MaintenanceReminderDto.fromDomain(reminder, userId)
            val updatedDto = getPostgrest().from("maintenance_reminders").update(dto) {
                filter {
                    eq("id", reminder.id)
                    eq("user_id", userId)
                }
                select()
            }.decodeSingle<MaintenanceReminderDto>()

            AppResult.Success(updatedDto.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal memperbarui pengingat"), e))
        }
    }

    override suspend fun markAsCompleted(id: String, completed: Boolean): AppResult<MaintenanceReminder> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val completedAtIso = if (completed) Instant.now().toString() else null
            val updatedDto = getPostgrest().from("maintenance_reminders").update({
                set("is_completed", completed)
                set("completed_at", completedAtIso)
            }) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
                select()
            }.decodeSingle<MaintenanceReminderDto>()

            AppResult.Success(updatedDto.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal memperbarui status selesai"), e))
        }
    }

    override suspend fun deleteReminder(id: String): AppResult<Unit> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            getPostgrest().from("maintenance_reminders").delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(sanitizeError(e, "Gagal menghapus pengingat"), e))
        }
    }
}
