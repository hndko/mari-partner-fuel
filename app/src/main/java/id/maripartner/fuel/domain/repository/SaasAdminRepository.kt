package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.GrantProResult
import id.maripartner.fuel.domain.model.SaasAdminMetrics

/**
 * Kontrak antarmuka repositori untuk Panel Pemilik / SaaS Owner Dashboard.
 */
interface SaasAdminRepository {

    /**
     * Memanggil RPC PostgreSQL get_saas_owner_metrics untuk mengambil seluruh metrik analitik.
     * @param timeframe Opsi rentang waktu: '7d', '30d', atau 'all'.
     */
    suspend fun getSaasMetrics(timeframe: String = "30d"): AppResult<SaasAdminMetrics>

    /**
     * Memanggil RPC PostgreSQL admin_grant_user_pro untuk memberikan atau memperpanjang status Pro manual.
     * @param targetUserId ID akun pengguna yang akan di-upgrade.
     * @param durationDays Durasi tambahan dalam hari (misal 30, 90, 365).
     * @param notes Catatan pemberian hak istimewa Pro.
     */
    suspend fun grantUserPro(
        targetUserId: String,
        durationDays: Int,
        notes: String? = null
    ): AppResult<GrantProResult>
}
