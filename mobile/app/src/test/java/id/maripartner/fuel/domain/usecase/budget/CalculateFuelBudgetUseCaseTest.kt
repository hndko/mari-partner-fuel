package id.maripartner.fuel.domain.usecase.budget

import id.maripartner.fuel.domain.model.BudgetHealthStatus
import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CalculateFuelBudgetUseCaseTest {

    private lateinit var useCase: CalculateFuelBudgetUseCase

    @Before
    fun setUp() {
        useCase = CalculateFuelBudgetUseCase()
    }

    @Test
    fun `budget not set returns isBudgetSet false`() {
        val result = useCase(
            budgetLimitIdr = 0L,
            transactions = emptyList()
        )

        assertFalse(result.isBudgetSet)
        assertEquals(0L, result.budgetLimitIdr)
        assertEquals(0L, result.remainingBudgetIdr)
    }

    @Test
    fun `budget within safe threshold calculates remaining and healthy burn rate`() {
        val txs = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 200_000L, volumeLiters = 20.0, transactionAt = Instant.now()),
            FuelTransaction(id = "2", vehicleId = "v1", totalAmountIdr = 300_000L, volumeLiters = 30.0, transactionAt = Instant.now())
        )

        val fixedDate = LocalDate.of(2026, 10, 10)
        val result = useCase(
            budgetLimitIdr = 1_500_000L,
            transactions = txs,
            currentDate = fixedDate
        )

        assertTrue(result.isBudgetSet)
        assertEquals(500_000L, result.totalSpentIdr)
        assertEquals(1_000_000L, result.remainingBudgetIdr)
        assertEquals(33.33f, result.spentPercentage, 0.5f)
        assertEquals(50_000L, result.dailyBurnRateIdr) // 500k / 10 days
        assertEquals(BudgetHealthStatus.EXCELLENT, result.healthStatus)
        assertFalse(result.isOverBudget)
    }

    @Test
    fun `burn rate exceeds month end predicts early runout day`() {
        // Pada tanggal 15 sudah menghabiskan 900.000 dari budget 1.000.000 (burn rate 60.000/hari)
        val txs = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 900_000L, volumeLiters = 90.0, transactionAt = Instant.now())
        )

        val fixedDate = LocalDate.of(2026, 10, 15) // total days 31
        val result = useCase(
            budgetLimitIdr = 1_000_000L,
            transactions = txs,
            currentDate = fixedDate
        )

        assertEquals(100_000L, result.remainingBudgetIdr)
        assertEquals(60_000L, result.dailyBurnRateIdr)
        // 15 + (100k / 60k) = 15 + 1 = 16 (habis tgl 16!)
        assertEquals(16, result.projectedRunoutDay)
        assertTrue(result.willRunoutBeforeMonthEnd)
        assertEquals(BudgetHealthStatus.CRITICAL, result.healthStatus)
    }

    @Test
    fun `over budget detects exceeded status`() {
        val txs = listOf(
            FuelTransaction(id = "1", vehicleId = "v1", totalAmountIdr = 1_200_000L, volumeLiters = 100.0, transactionAt = Instant.now())
        )

        val result = useCase(
            budgetLimitIdr = 1_000_000L,
            transactions = txs,
            currentDate = LocalDate.of(2026, 10, 20)
        )

        assertTrue(result.isOverBudget)
        assertEquals(0L, result.remainingBudgetIdr)
        assertEquals(BudgetHealthStatus.OVER_BUDGET, result.healthStatus)
    }
}
