package id.maripartner.fuel.domain.model.owner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model metrik utama SaaS untuk Panel Pemilik (Owner Console).
 */
@Serializable
data class SaasAdminMetrics(
    @SerialName("success")
    val success: Boolean = true,
    @SerialName("timeframe")
    val timeframe: String = "30d",
    @SerialName("kpi")
    val kpi: SaasKpi = SaasKpi(),
    @SerialName("growth_trend")
    val growthTrend: List<DailyGrowthPoint> = emptyList(),
    @SerialName("conversion_stats")
    val conversionStats: PlanConversionStats = PlanConversionStats(),
    @SerialName("recent_users")
    val recentUsers: List<AdminUserSummary> = emptyList(),
    @SerialName("generated_at")
    val generatedAt: String? = null
)

/**
 * Kartu Ringkasan Indikator Kinerja Utama (KPI) SaaS.
 */
@Serializable
data class SaasKpi(
    @SerialName("total_users")
    val totalUsers: Long = 0L,
    @SerialName("active_pro_users")
    val activeProUsers: Long = 0L,
    @SerialName("free_users")
    val freeUsers: Long = 0L,
    @SerialName("total_revenue_idr")
    val totalRevenueIdr: Long = 0L,
    @SerialName("total_transactions_recorded")
    val totalTransactionsRecorded: Long = 0L,
    @SerialName("total_vehicles_registered")
    val totalVehiclesRegistered: Long = 0L
)

/**
 * Titik data tren registrasi pendaftaran harian.
 */
@Serializable
data class DailyGrowthPoint(
    @SerialName("date_label")
    val dateLabel: String = "",
    @SerialName("count")
    val count: Long = 0L
)

/**
 * Statistik konversi paket Free ke Pro.
 */
@Serializable
data class PlanConversionStats(
    @SerialName("free_users")
    val freeUsers: Long = 0L,
    @SerialName("pro_monthly_users")
    val proMonthlyUsers: Long = 0L,
    @SerialName("pro_yearly_users")
    val proYearlyUsers: Long = 0L,
    @SerialName("total_pro_users")
    val totalProUsers: Long = 0L,
    @SerialName("conversion_rate_percent")
    val conversionRatePercent: Double = 0.0
)

/**
 * Ringkasan profil akun pengguna untuk manajemen dan monitoring pemilik.
 */
@Serializable
data class AdminUserSummary(
    @SerialName("id")
    val id: String,
    @SerialName("display_name")
    val displayName: String = "Pengguna Mari",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("role")
    val role: String = "user",
    @SerialName("is_pro")
    val isPro: Boolean = false,
    @SerialName("plan_name")
    val planName: String = "Mari Free",
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("vehicles_count")
    val vehiclesCount: Long = 0L,
    @SerialName("transactions_count")
    val transactionsCount: Long = 0L
)

/**
 * DTO respons saat Pemilik memberikan status Pro manual kepada pengguna.
 */
@Serializable
data class GrantProResult(
    @SerialName("success")
    val success: Boolean = true,
    @SerialName("is_extended")
    val isExtended: Boolean = false,
    @SerialName("is_permanent")
    val isPermanent: Boolean = false,
    @SerialName("target_user_id")
    val targetUserId: String = "",
    @SerialName("new_expires_at")
    val newExpiresAt: String? = null,
    @SerialName("duration_days_added")
    val durationDaysAdded: Int? = 30,
    @SerialName("message")
    val message: String = ""
)
