package id.maripartner.fuel.domain.repository.owner

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.owner.AdminUserSummary
import id.maripartner.fuel.domain.model.owner.GrantProResult
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx
import id.maripartner.fuel.domain.model.owner.OwnerPlanItem
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics

/**
 * Kontrak antarmuka repositori khusus fungsionalitas Pemilik SaaS (Owner Console).
 */
interface OwnerRepository {

    /**
     * Mengambil metrik ringkasan eksekutif SaaS (KPI, tren pertumbuhan registrasi, rasio konversi).
     */
    suspend fun getSaasMetrics(timeframe: String = "30d"): AppResult<SaasAdminMetrics>

    /**
     * Mengambil seluruh katalog paket langganan langsung dari tabel subscription_plans.
     */
    suspend fun getSubscriptionPlans(): AppResult<List<OwnerPlanItem>>

    /**
     * Memperbarui konfigurasi paket langganan (nama, harga IDR, kuota AI, batas kendaraan, status aktif, dan harga promo).
     */
    suspend fun updateSubscriptionPlan(
        planId: String,
        name: String,
        priceIdr: Long,
        aiScanQuota: Int,
        maxVehicles: Int?,
        isActive: Boolean,
        description: String? = null,
        isPromoActive: Boolean = false,
        promoPriceIdr: Long? = null,
        promoMaxPerUser: Int? = null,
        promoLabel: String = "PROMO SPESIAL"
    ): AppResult<String>

    /**
     * Memberikan atau memperpanjang status langganan Pro pengguna secara manual.
     */
    suspend fun grantUserPro(
        targetUserId: String,
        durationDays: Int,
        notes: String? = null
    ): AppResult<GrantProResult>

    /**
     * Mengambil daftar log transaksi pembayaran masuk untuk audit finansial pemilik.
     */
    suspend fun getPaymentTransactions(limit: Int = 50): AppResult<List<OwnerPaymentTx>>
}
