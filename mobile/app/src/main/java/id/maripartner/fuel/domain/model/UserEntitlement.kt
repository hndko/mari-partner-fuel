package id.maripartner.fuel.domain.model

import java.time.Duration
import java.time.Instant

data class UserEntitlement(
    val userId: String = "",
    val isPro: Boolean = false,
    val planCode: String = "free",
    val planName: String = "Mari Free",
    val status: String = "active",
    val expiresAt: Instant? = null,
    val autoRenew: Boolean = false,
    val cancelAtPeriodEnd: Boolean = false,
    val maxVehicles: Int? = 2,
    val features: List<String> = emptyList(),
    val aiQuota: AiQuotaStatus = AiQuotaStatus(),
    val isExpiringSoon: Boolean = false,
    val daysUntilExpiry: Int? = null,
    val isRecentlyExpired: Boolean = false
) {
    fun hasFeature(feature: String): Boolean = features.contains(feature)

    fun canAddVehicle(currentActiveVehiclesCount: Int): Boolean {
        val limit = maxVehicles ?: return true
        return currentActiveVehiclesCount < limit
    }

    val isGracePeriod: Boolean
        get() = status.equals("grace_period", ignoreCase = true)

    val isExpired: Boolean
        get() = status.equals("expired", ignoreCase = true) || (expiresAt != null && Instant.now().isAfter(expiresAt))

    /**
     * Menghitung sisa hari secara dinamis jika tidak dikirim dari server
     */
    val effectiveDaysUntilExpiry: Int?
        get() = daysUntilExpiry ?: run {
            if (!isPro || expiresAt == null) return@run null
            val now = Instant.now()
            if (now.isAfter(expiresAt)) return@run 0
            Duration.between(now, expiresAt).toDays().toInt().coerceAtLeast(0)
        }
}
