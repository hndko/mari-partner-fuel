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
    val description: String? = null,
    val isPromoActive: Boolean = false,
    val promoPriceIdr: Long? = null,
    val promoMaxPerUser: Int? = null,
    val promoLabel: String = "PROMO SPESIAL",
    val userPromoPurchasesCount: Int = 0,
    val isUserEligibleForPromo: Boolean = false
) {
    val isFree: Boolean
        get() = code.equals("free", ignoreCase = true) || priceIdr == 0L

    val isPro: Boolean
        get() = !isFree

    val isYearly: Boolean
        get() = billingPeriod.equals("yearly", ignoreCase = true)

    val isMonthly: Boolean
        get() = billingPeriod.equals("monthly", ignoreCase = true)

    /**
     * Menghitung harga efektif yang berlaku:
     * Menggunakan harga promo jika promo aktif dan pengguna masih memiliki kuota/hak klaim promo.
     */
    val effectivePriceIdr: Long
        get() = if (isPromoActive && isUserEligibleForPromo && promoPriceIdr != null) {
            promoPriceIdr
        } else {
            priceIdr
        }

    /**
     * Memeriksa apakah promo sedang aktif dan pengguna berhak mendapatkan diskon promo
     */
    val hasActivePromoDiscount: Boolean
        get() = isPromoActive && isUserEligibleForPromo && promoPriceIdr != null && promoPriceIdr < priceIdr
}
