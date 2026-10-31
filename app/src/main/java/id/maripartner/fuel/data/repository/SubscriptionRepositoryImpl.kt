package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.SubscriptionPlanDto
import id.maripartner.fuel.data.remote.dto.SubscriptionRecordDto
import id.maripartner.fuel.data.remote.dto.UserEntitlementDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.AiQuotaStatus
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant

class SubscriptionRepositoryImpl(
    private val functions: Functions? = null,
    private val postgrest: Postgrest? = null,
    private val auth: Auth? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubscriptionRepository {

    private fun getFunctions(): Functions = functions ?: SupabaseClientProvider.functions
    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest
    private fun getAuth(): Auth = auth ?: SupabaseClientProvider.auth

    @Volatile
    private var cachedEntitlement: UserEntitlement? = null

    override fun clearCache() {
        cachedEntitlement = null
    }

    override suspend fun getMyEntitlement(forceRefresh: Boolean): AppResult<UserEntitlement> = withContext(dispatcher) {
        if (!forceRefresh && cachedEntitlement != null) {
            return@withContext AppResult.Success(cachedEntitlement!!)
        }

        val currentUserId = getAuth().currentUserOrNull()?.id

        // Tingkat 1 (Autoritatif Langsung): Query tabel subscriptions via PostgREST
        if (!currentUserId.isNullOrBlank()) {
            try {
                val subs = getPostgrest().from("subscriptions").select(
                    Columns.raw("id, user_id, plan_id, status, starts_at, expires_at, auto_renew, cancel_at_period_end, provider, subscription_plans(*)")
                ) {
                    filter {
                        eq("user_id", currentUserId)
                        eq("status", "active")
                    }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }.decodeList<SubscriptionRecordDto>()

                if (subs.isNotEmpty()) {
                    val activeSub = subs.first()
                    val expiresInstant = activeSub.expiresAt?.let {
                        runCatching { Instant.parse(it) }.getOrNull()
                    }
                    val isStillActive = expiresInstant == null || expiresInstant.isAfter(Instant.now())
                    if (isStillActive && activeSub.subscriptionPlan != null) {
                        val plan = activeSub.subscriptionPlan
                        val isProPlan = !plan.code.equals("free", ignoreCase = true)
                        val entitlement = UserEntitlement(
                            userId = currentUserId,
                            isPro = isProPlan,
                            planCode = plan.code,
                            planName = plan.name,
                            status = activeSub.status,
                            expiresAt = expiresInstant,
                            autoRenew = activeSub.autoRenew,
                            cancelAtPeriodEnd = activeSub.cancelAtPeriodEnd,
                            maxVehicles = plan.maxVehicles,
                            features = plan.getFeaturesList(),
                            aiQuota = AiQuotaStatus(
                                limit = plan.aiScanQuota,
                                used = 0,
                                remaining = plan.aiScanQuota
                            )
                        )
                        cachedEntitlement = entitlement
                        return@withContext AppResult.Success(entitlement)
                    }
                }
            } catch (_: Exception) { }

            // Tingkat 2: RPC get_user_effective_entitlement
            try {
                val params = buildJsonObject {
                    put("p_user_id", currentUserId)
                }
                val rpcResult = getPostgrest().rpc("get_user_effective_entitlement", params)
                val jsonElement = json.parseToJsonElement(rpcResult.data)
                val jsonObj = jsonElement.jsonObject
                val isPro = jsonObj["is_pro"]?.jsonPrimitive?.booleanOrNull ?: false
                val planCode = jsonObj["plan_code"]?.jsonPrimitive?.contentOrNull ?: "free"
                val planName = jsonObj["plan_name"]?.jsonPrimitive?.contentOrNull ?: "Mari Free"
                val expiresStr = jsonObj["expires_at"]?.jsonPrimitive?.contentOrNull
                val expiresInstant = expiresStr?.let { runCatching { Instant.parse(it) }.getOrNull() }
                val cancelAtPeriodEnd = jsonObj["cancel_at_period_end"]?.jsonPrimitive?.booleanOrNull ?: false
                val maxVehicles = jsonObj["max_vehicles"]?.jsonPrimitive?.intOrNull
                val aiQuotaLimit = jsonObj["ai_scan_quota"]?.jsonPrimitive?.intOrNull ?: 5
                val features = jsonObj["features"]?.jsonArray?.mapNotNull { runCatching { it.jsonPrimitive.content }.getOrNull() } ?: emptyList()
                val isExpiringSoon = jsonObj["is_expiring_soon"]?.jsonPrimitive?.booleanOrNull ?: false
                val daysUntilExpiry = jsonObj["days_until_expiry"]?.jsonPrimitive?.intOrNull
                val isRecentlyExpired = jsonObj["is_recently_expired"]?.jsonPrimitive?.booleanOrNull ?: false

                val entitlement = UserEntitlement(
                    userId = currentUserId,
                    isPro = isPro,
                    planCode = planCode,
                    planName = planName,
                    status = "active",
                    expiresAt = expiresInstant,
                    autoRenew = false,
                    cancelAtPeriodEnd = cancelAtPeriodEnd,
                    maxVehicles = maxVehicles,
                    features = features,
                    aiQuota = AiQuotaStatus(
                        limit = aiQuotaLimit,
                        used = 0,
                        remaining = aiQuotaLimit
                    ),
                    isExpiringSoon = isExpiringSoon,
                    daysUntilExpiry = daysUntilExpiry,
                    isRecentlyExpired = isRecentlyExpired
                )
                cachedEntitlement = entitlement
                return@withContext AppResult.Success(entitlement)
            } catch (_: Exception) { }
        }

        // Tingkat 3: Edge Function get-my-entitlements (jika terdeploy)
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
                userId = dto.userId.ifBlank { currentUserId.orEmpty() },
                isPro = dto.isPro,
                planCode = dto.planCode,
                planName = dto.planName,
                status = dto.status,
                expiresAt = expiresInstant,
                autoRenew = dto.autoRenew,
                cancelAtPeriodEnd = dto.cancelAtPeriodEnd,
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
            return@withContext AppResult.Success(entitlement)
        } catch (_: Exception) { }

        // Jika ada cache lokal sebelumnya, gunakan
        cachedEntitlement?.let {
            return@withContext AppResult.Success(it)
        }

        // Fallback aman ke Free jika offline dan belum ada data
        val fallbackFree = UserEntitlement(
            userId = currentUserId.orEmpty(),
            isPro = false,
            planCode = "free",
            planName = "Mari Free",
            status = "active",
            maxVehicles = 2,
            features = listOf("manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"),
            aiQuota = AiQuotaStatus(limit = 5, used = 0, remaining = 5)
        )
        cachedEntitlement = fallbackFree
        AppResult.Success(fallbackFree)
    }

    override suspend fun getSubscriptionPlans(): AppResult<List<SubscriptionPlan>> = withContext(dispatcher) {
        try {
            val currentUserId = getAuth().currentUserOrNull()?.id
            val dtos: List<SubscriptionPlanDto> = try {
                val params = buildJsonObject {
                    if (!currentUserId.isNullOrBlank()) {
                        put("p_user_id", currentUserId)
                    }
                }
                getPostgrest().rpc("get_available_subscription_plans", params).decodeList<SubscriptionPlanDto>()
            } catch (_: Exception) {
                // Fallback ke select langsung dari tabel subscription_plans
                getPostgrest().from("subscription_plans").select {
                    filter {
                        eq("is_active", true)
                    }
                }.decodeList<SubscriptionPlanDto>()
            }

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
                    description = dto.description,
                    isPromoActive = dto.isPromoActive,
                    promoPriceIdr = dto.promoPriceIdr,
                    promoMaxPerUser = dto.promoMaxPerUser,
                    promoLabel = dto.promoLabel,
                    userPromoPurchasesCount = dto.userPromoPurchasesCount,
                    isUserEligibleForPromo = dto.isUserEligibleForPromo || (dto.isPromoActive && dto.promoPriceIdr != null && (dto.promoMaxPerUser == null || dto.promoMaxPerUser <= 0 || dto.userPromoPurchasesCount < dto.promoMaxPerUser))
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

    override suspend fun requestDowngradeToFree(): AppResult<String> = withContext(dispatcher) {
        val currentUserId = getAuth().currentUserOrNull()?.id
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum terautentikasi"))

        try {
            val params = buildJsonObject {
                put("p_user_id", currentUserId)
            }
            val rpcResult = getPostgrest().rpc("request_subscription_downgrade", params)
            val jsonElement = json.parseToJsonElement(rpcResult.data)
            val jsonObj = jsonElement.jsonObject
            val isSuccess = jsonObj["success"]?.jsonPrimitive?.booleanOrNull ?: false
            val message = jsonObj["message"]?.jsonPrimitive?.contentOrNull
                ?: jsonObj["error"]?.jsonPrimitive?.contentOrNull
                ?: "Permintaan downgrade ke paket Free berhasil diproses."

            if (isSuccess) {
                clearCache()
                getMyEntitlement(forceRefresh = true)
                AppResult.Success(message)
            } else {
                val errorMsg = jsonObj["error"]?.jsonPrimitive?.contentOrNull ?: "Gagal memproses permohonan downgrade."
                AppResult.Error(AppException.Unknown(errorMsg))
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Network(e.message ?: "Terjadi kesalahan saat memproses permohonan downgrade", e))
        }
    }
}
