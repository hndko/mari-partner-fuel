package id.maripartner.fuel.domain.usecase.budget

import id.maripartner.fuel.domain.model.BudgetHealthStatus
import id.maripartner.fuel.domain.model.FuelBudgetReport
import id.maripartner.fuel.domain.model.FuelTransaction
import java.time.LocalDate

class CalculateFuelBudgetUseCase {

    operator fun invoke(
        budgetLimitIdr: Long,
        transactions: List<FuelTransaction>,
        currentDate: LocalDate = LocalDate.now()
    ): FuelBudgetReport {
        if (budgetLimitIdr <= 0L) {
            val totalSpent = transactions.sumOf { it.totalAmountIdr }
            return FuelBudgetReport(
                budgetLimitIdr = 0L,
                totalSpentIdr = totalSpent,
                remainingBudgetIdr = 0L,
                spentPercentage = 0f,
                isBudgetSet = false
            )
        }

        val totalSpent = transactions.sumOf { it.totalAmountIdr }
        val remaining = (budgetLimitIdr - totalSpent).coerceAtLeast(0L)
        val spentPercent = (totalSpent.toDouble() / budgetLimitIdr.toDouble() * 100.0).toFloat()

        val dayOfMonth = currentDate.dayOfMonth
        val totalDaysInMonth = currentDate.lengthOfMonth()
        val daysRemaining = (totalDaysInMonth - dayOfMonth).coerceAtLeast(0)

        val dailyBurnRate = if (dayOfMonth > 0) totalSpent / dayOfMonth else totalSpent
        val projectedEndMonthSpent = totalSpent + (dailyBurnRate * daysRemaining)

        val projectedRunoutDay: Int? = if (dailyBurnRate > 0 && remaining > 0) {
            val daysUntilEmpty = (remaining / dailyBurnRate).toInt()
            val runout = dayOfMonth + daysUntilEmpty
            if (runout <= totalDaysInMonth) runout else null
        } else if (remaining == 0L && totalSpent > 0) {
            dayOfMonth
        } else {
            null
        }

        val health = when {
            totalSpent > budgetLimitIdr -> BudgetHealthStatus.OVER_BUDGET
            spentPercent >= 90f -> BudgetHealthStatus.CRITICAL
            spentPercent >= 75f -> BudgetHealthStatus.WARNING
            spentPercent >= 50f -> BudgetHealthStatus.SAFE
            else -> BudgetHealthStatus.EXCELLENT
        }

        return FuelBudgetReport(
            budgetLimitIdr = budgetLimitIdr,
            totalSpentIdr = totalSpent,
            remainingBudgetIdr = remaining,
            spentPercentage = spentPercent,
            dailyBurnRateIdr = dailyBurnRate,
            daysPassedInMonth = dayOfMonth,
            daysRemainingInMonth = daysRemaining,
            projectedMonthEndSpentIdr = projectedEndMonthSpent,
            projectedRunoutDay = projectedRunoutDay,
            healthStatus = health,
            isBudgetSet = true
        )
    }
}
