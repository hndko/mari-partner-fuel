package id.maripartner.fuel.domain.model

enum class BudgetHealthStatus {
    EXCELLENT,  // < 50%
    SAFE,       // 50% - 75%
    WARNING,    // 76% - 90%
    CRITICAL,   // 91% - 100%
    OVER_BUDGET // > 100%
}

data class FuelBudgetReport(
    val budgetLimitIdr: Long = 0L,
    val totalSpentIdr: Long = 0L,
    val remainingBudgetIdr: Long = 0L,
    val spentPercentage: Float = 0f,
    val dailyBurnRateIdr: Long = 0L,
    val daysPassedInMonth: Int = 1,
    val daysRemainingInMonth: Int = 30,
    val projectedMonthEndSpentIdr: Long = 0L,
    val projectedRunoutDay: Int? = null, // Tanggal dalam bulan saat anggaran diperkirakan habis
    val healthStatus: BudgetHealthStatus = BudgetHealthStatus.SAFE,
    val isBudgetSet: Boolean = false
) {
    val isOverBudget: Boolean
        get() = totalSpentIdr > budgetLimitIdr && budgetLimitIdr > 0

    val willRunoutBeforeMonthEnd: Boolean
        get() = projectedRunoutDay != null && projectedRunoutDay <= (daysPassedInMonth + daysRemainingInMonth)
}
