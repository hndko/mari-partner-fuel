package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.SubscriptionPlanDto
import id.maripartner.fuel.data.remote.dto.UserEntitlementDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.AiQuotaStatus
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant

class SubscriptionRepositoryImpl(
    private val functions: Functions? = null,
    private val postgrest: Postgrest? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubscriptionRepository {

    private fun getFunctions(): Functions = functions ?: SupabaseClientProvider.functions
    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest

    @Volatile
    private var cachedEntitlement: UserEntitlement? = null

    override suspend fun getMyEntitlement(forceRefresh: Boolean): AppResult<UserEntitlement> = withContext(dispatcher) {
        if (!forceRefresh && cachedEntitlement != null) {
            return@withContext AppResult.Success(cachedEntitlement!!)
        }

        try {
            val response = getFunctions().invoke("get-my-entitlements")
            val text = response.bodyAsText()
            val dto = json.decodeFromString<UserEntitlementDto>(text)

            val expiresInstant = dto.expiresAt?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }
            val resetsInstant = dto.aiQuota.resetsAt?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }

            val entitlement = UserEntitlement(
                userId = dto.userId,
                isPro = dto.isPro,
                planCode = dto.planCode,
                planName = dto.planName,
                status = dto.status,
                expiresAt = expiresInstant,
                autoRenew = dto.autoRenew,
                maxVehicles = dto.maxVehicles,
                features = dto.features,
                aiQuota = AiQuotaStatus(
                    limit = dto.aiQuota.limit,
                    used = dto.aiQuota.used,
                    remaining = dto.aiQuota.remaining,
                    resetsAt = resetsInstant
                )
            )

            cachedEntitlement = entitlement
            AppResult.Success(entitlement)
        } catch (e: Exception) {
            // Jika ada cache lokal, gunakan untuk mencegah pemblokiran UI
            cachedEntitlement?.let {
                return@withContext AppResult.Success(it)
            }

            // Fallback aman ke Free jika jaringan belum stabil
            val fallbackFree = UserEntitlement(
                isPro = false,
                planCode = "free",
                planName = "Mari Free",
                status = "active",
                maxVehicles = 2,
                features = listOf("manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"),
                aiQuota = AiQuotaStatus(limit = 5, used = 0, remaining = 5)
            )
            AppResult.Success(fallbackFree)
        }
    }

    override suspend fun getSubscriptionPlans(): AppResult<List<SubscriptionPlan>> = withContext(dispatcher) {
        try {
            val dtos = getPostgrest().from("subscription_plans").select {
                filter {
                    eq("is_active", true)
                }
            }.decodeList<SubscriptionPlanDto>()

            val plans = dtos.map { dto ->
                SubscriptionPlan(
                    id = dto.id,
                    code = dto.code,
                    name = dto.name,
                    billingPeriod = dto.billingPeriod,
                    priceIdr = dto.priceIdr,
                    aiScanQuota = dto.aiScanQuota,
                    maxVehicles = dto.maxVehicles,
                    isActive = dto.isActive,
                    features = dto.getFeaturesList(),
                    description = dto.description
                )
            }.sortedBy { it.priceIdr }

            if (plans.isNotEmpty()) {
                AppResult.Success(plans)
            } else {
                AppResult.Success(getDefaultPlansCatalog())
            }
        } catch (_: Exception) {
            // Fallback ke katalog resmi backend default jika offline
            AppResult.Success(getDefaultPlansCatalog())
        }
    }

    override suspend fun getCachedEntitlement(): UserEntitlement? {
        return cachedEntitlement
    }

    private fun getDefaultPlansCatalog(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "plan_free",
                code = "free",
                name = "Mari Free",
                billingPeriod = "free",
                priceIdr = 0L,
                aiScanQuota = 5,
                maxVehicles = 2,
                isActive = true,
                features = listOf("manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"),
                description = "Paket esensial gratis untuk 2 kendaraan aktif dan 5 pemindaian struk AI per bulan."
            ),
            SubscriptionPlan(
                id = "plan_pro_monthly",
                code = "pro_monthly",
                name = "Mari Pro Bulanan",
                billingPeriod = "monthly",
                priceIdr = 29000L,
                aiScanQuota = 100,
                maxVehicles = null,
                isActive = true,
                features = listOf("manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles", "ai_scans_100", "advanced_analytics", "export_csv", "priority_support"),
                description = "Akses penuh Mari Pro bulanan tanpa batas kendaraan, 100 scan AI, dan analitik lengkap."
            ),
            SubscriptionPlan(
                id = "plan_pro_yearly",
                code = "pro_yearly",
                name = "Mari Pro Tahunan",
                billingPeriod = "yearly",
                priceIdr = 290000L,
                aiScanQuota = 100,
                maxVehicles = null,
                isActive = true,
                features = listOf("manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles", "ai_scans_100", "advanced_analytics", "export_csv", "priority_support", "discount_2_months"),
                description = "Hemat 17% dengan paket tahunan. Nikmati seluruh fitur Pro tanpa batas kendaraan."
            )
        )
    }
}
