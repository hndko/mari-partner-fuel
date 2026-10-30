package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.GrantProResult
import id.maripartner.fuel.domain.model.SaasAdminMetrics
import id.maripartner.fuel.domain.repository.SaasAdminRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SaasAdminRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SaasAdminRepository {

    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest

    override suspend fun getSaasMetrics(timeframe: String): AppResult<SaasAdminMetrics> = withContext(dispatcher) {
        try {
            val params = buildJsonObject {
                put("p_timeframe", timeframe)
            }
            val result = getPostgrest().rpc("get_saas_owner_metrics", params)
            val metrics = json.decodeFromString<SaasAdminMetrics>(result.data)
            AppResult.Success(metrics)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val sanitized = when {
                msg.contains("Akses ditolak") || msg.contains("42501") -> {
                    AppException.Authentication("Akses ditolak: Hanya akun berstatus Owner/Admin di database yang diizinkan mengakses panel ini.")
                }
                msg.contains("Could not find the function", ignoreCase = true) -> {
                    AppException.Network("Fungsi analitik belum aktif di Supabase. Silakan jalankan berkas migrasi SQL (Query 10) di Supabase SQL Editor.")
                }
                else -> {
                    val cleanMsg = msg.substringBefore("URL:").substringBefore("Headers:").trim()
                    AppException.Network(cleanMsg.ifBlank { "Gagal memuat analitik SaaS dari server" })
                }
            }
            AppResult.Error(sanitized)
        }
    }

    override suspend fun grantUserPro(
        targetUserId: String,
        durationDays: Int,
        notes: String?
    ): AppResult<GrantProResult> = withContext(dispatcher) {
        try {
            val params = buildJsonObject {
                put("p_target_user_id", targetUserId)
                put("p_duration_days", durationDays)
                if (notes != null) {
                    put("p_notes", notes)
                }
            }
            val result = getPostgrest().rpc("admin_grant_user_pro", params)
            val grantResult = json.decodeFromString<GrantProResult>(result.data)
            AppResult.Success(grantResult)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val sanitized = when {
                msg.contains("Akses ditolak") || msg.contains("42501") -> {
                    AppException.Authentication("Akses ditolak: Hanya Owner yang diizinkan memberikan status Pro.")
                }
                msg.contains("Could not find the function", ignoreCase = true) -> {
                    AppException.Network("Fungsi database admin_grant_user_pro belum aktif di Supabase. Silakan jalankan migrasi SQL (Query 10).")
                }
                else -> {
                    val cleanMsg = msg.substringBefore("URL:").substringBefore("Headers:").trim()
                    AppException.Network(cleanMsg.ifBlank { "Gagal memperbarui status Pro pengguna di server" })
                }
            }
            AppResult.Error(sanitized)
        }
    }
}
