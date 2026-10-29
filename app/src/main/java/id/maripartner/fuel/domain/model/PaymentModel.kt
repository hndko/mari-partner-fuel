package id.maripartner.fuel.domain.model

import java.time.Instant

enum class PaymentProviderType(val id: String, val displayName: String) {
    GOOGLE_PLAY("google_play", "Google Play Billing (Resmi Play Store)"),
    QRISPY("qrispy", "QRIS / E-Wallet (Distribusi Web & Direct)")
}

data class PaymentOrder(
    val paymentReference: String,
    val provider: String,
    val amountIdr: Long,
    val currency: String = "IDR",
    val planCode: String,
    val planName: String,
    val billingPeriod: String,
    val productId: String? = null,
    val basePlanId: String? = null,
    val checkoutUrl: String? = null,
    val qrisImageUrl: String? = null,
    val expiresAt: Instant? = null
)

data class PaymentStatus(
    val paymentReference: String,
    val provider: String = "",
    val amountIdr: Long = 0L,
    val status: String = "pending", // "created", "pending", "paid", "expired", "failed"
    val isPaid: Boolean = false,
    val isPro: Boolean = false,
    val paidAt: Instant? = null,
    val expiresAt: Instant? = null
) {
    val isPending: Boolean
        get() = status.equals("pending", ignoreCase = true) || status.equals("created", ignoreCase = true)

    val isExpired: Boolean
        get() = status.equals("expired", ignoreCase = true) || (expiresAt != null && Instant.now().isAfter(expiresAt))

    val isFailed: Boolean
        get() = status.equals("failed", ignoreCase = true) || status.equals("cancelled", ignoreCase = true)
}
