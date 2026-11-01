package id.maripartner.fuel.domain.model

import java.time.Instant

data class AiQuotaStatus(
    val limit: Int = 5,
    val used: Int = 0,
    val remaining: Int = 5,
    val resetsAt: Instant? = null
) {
    val usagePercent: Float
        get() = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 1f

    val isExhausted: Boolean
        get() = remaining <= 0
}
