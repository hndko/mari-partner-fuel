package id.maripartner.fuel.domain.model

data class SubscriptionPlan(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val billingPeriod: String = "monthly", // 'free', 'monthly', 'yearly'
    val priceIdr: Long = 0L,
    val aiScanQuota: Int = 0,
    val maxVehicles: Int? = null,
    val isActive: Boolean = true,
    val features: List<String> = emptyList(),
    val description: String? = null
) {
    val isFree: Boolean
        get() = code.equals("free", ignoreCase = true) || priceIdr == 0L

    val isPro: Boolean
        get() = !isFree

    val isYearly: Boolean
        get() = billingPeriod.equals("yearly", ignoreCase = true)

    val isMonthly: Boolean
        get() = billingPeriod.equals("monthly", ignoreCase = true)
}
