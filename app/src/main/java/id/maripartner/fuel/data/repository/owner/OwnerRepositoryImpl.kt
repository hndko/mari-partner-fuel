package id.maripartner.fuel.data.repository.owner

import android.util.Log
import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.owner.GrantProResult
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx
import id.maripartner.fuel.domain.model.owner.OwnerPlanItem
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics
import id.maripartner.fuel.domain.repository.owner.OwnerRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OwnerRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OwnerRepository {

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
            AppResult.Error(sanitizeException(e, "Gagal memuat analitik SaaS"))
        }
    }

    override suspend fun getSubscriptionPlans(): AppResult<List<OwnerPlanItem>> = withContext(dispatcher) {
        try {
            val plans = getPostgrest()["subscription_plans"]
                .select {
                    order("price_idr", Order.ASCENDING)
                }
                .decodeList<OwnerPlanItem>()
            AppResult.Success(plans)
        } catch (e: Exception) {
            AppResult.Error(sanitizeException(e, "Gagal memuat daftar paket langganan"))
        }
    }

    override suspend fun updateSubscriptionPlan(
        planId: String,
        name: String,
        priceIdr: Long,
        aiScanQuota: Int,
        maxVehicles: Int?,
        isActive: Boolean,
        description: String?,
        isPromoActive: Boolean,
        promoPriceIdr: Long?,
        promoMaxPerUser: Int?,
        promoLabel: String
    ): AppResult<String> = withContext(dispatcher) {
        try {
            val params = buildJsonObject {
                put("p_plan_id", planId)
                put("p_name", name.trim())
                put("p_price_idr", priceIdr)
                put("p_ai_scan_quota", aiScanQuota)
                if (maxVehicles != null) {
                    put("p_max_vehicles", maxVehicles)
                } else {
                    put("p_max_vehicles", JsonNull)
                }
                put("p_is_active", isActive)
                if (description != null) {
                    put("p_description", description.trim())
                } else {
                    put("p_description", JsonNull)
                }
                put("p_is_promo_active", isPromoActive)
                if (promoPriceIdr != null) {
                    put("p_promo_price_idr", promoPriceIdr)
                } else {
                    put("p_promo_price_idr", JsonNull)
                }
                if (promoMaxPerUser != null) {
                    put("p_promo_max_per_user", promoMaxPerUser)
                } else {
                    put("p_promo_max_per_user", JsonNull)
                }
                put("p_promo_label", promoLabel.trim().ifBlank { "PROMO SPESIAL" })
            }
            Log.d("OwnerRepo", "Calling admin_update_subscription_plan with params: $params")
            val result = getPostgrest().rpc("admin_update_subscription_plan", params)
            Log.d("OwnerRepo", "admin_update_subscription_plan response data: ${result.data}")
            val jsonObj = json.parseToJsonElement(result.data)
            val message = jsonObj.toString()
            AppResult.Success("Konfigurasi paket '$name' berhasil disimpan.")
        } catch (e: Exception) {
            Log.e("OwnerRepo", "admin_update_subscription_plan failed", e)
            AppResult.Error(sanitizeException(e, "Gagal memperbarui konfigurasi paket"))
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
                    put("p_notes", notes.trim())
                }
            }
            val result = getPostgrest().rpc("admin_grant_user_pro", params)
            val grantResult = json.decodeFromString<GrantProResult>(result.data)
            AppResult.Success(grantResult)
        } catch (e: Exception) {
            AppResult.Error(sanitizeException(e, "Gagal memberikan status Pro"))
        }
    }

    override suspend fun getPaymentTransactions(limit: Int): AppResult<List<OwnerPaymentTx>> = withContext(dispatcher) {
        try {
            val params = buildJsonObject {
                put("p_limit", limit)
            }
            val result = getPostgrest().rpc("admin_get_payment_transactions", params)
            val response = json.decodeFromString<OwnerPaymentTxResponse>(result.data)
            AppResult.Success(response.transactions)
        } catch (e: Exception) {
            AppResult.Error(sanitizeException(e, "Gagal memuat log transaksi pembayaran"))
        }
    }

    private fun sanitizeException(e: Exception, defaultPrefix: String): AppException {
        val msg = e.message ?: ""
        return when {
            msg.contains("Akses ditolak") || msg.contains("42501") -> {
                AppException.Authentication("Akses ditolak: Hanya akun berstatus Owner yang diizinkan melakukan tindakan ini.")
            }
            msg.contains("Could not find the function", ignoreCase = true) -> {
                AppException.Network("Fungsi database analitik belum aktif di Supabase. Silakan jalankan berkas migrasi SQL (Query 10/11).")
            }
            else -> {
                val cleanMsg = msg.substringBefore("URL:").substringBefore("Headers:").trim()
                AppException.Network(cleanMsg.ifBlank { "$defaultPrefix dari server" })
            }
        }
    }
}

@Serializable
private data class OwnerPaymentTxResponse(
    val success: Boolean = true,
    val transactions: List<OwnerPaymentTx> = emptyList()
)
