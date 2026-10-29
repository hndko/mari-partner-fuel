package id.maripartner.fuel.domain.usecase.dashboard

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.DashboardSummary
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.model.VehicleSpendingSummary
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import java.time.LocalDate
import java.time.YearMonth

/**
 * Use case untuk menyusun ringkasan statistik Dashboard pengguna secara akurat
 * berbasis data transaksi nyata dari repositori Supabase.
 */
class GetDashboardSummaryUseCase(
    private val transactionRepository: FuelTransactionRepository
) {

    suspend operator fun invoke(
        referenceDate: LocalDate = LocalDate.now()
    ): AppResult<DashboardSummary> {
        val currentYearMonth = YearMonth.from(referenceDate)
        val currentStart = currentYearMonth.atDay(1)
        val currentEnd = currentYearMonth.atEndOfMonth()

        val prevYearMonth = currentYearMonth.minusMonths(1)
        val prevStart = prevYearMonth.atDay(1)
        val prevEnd = prevYearMonth.atEndOfMonth()

        // 1. Ambil transaksi bulan berjalan
        val currentFilter = TransactionFilter(
            startDate = currentStart,
            endDate = currentEnd,
            sortOrder = SortOrder.NEWEST_FIRST
        )
        val currentResult = transactionRepository.getTransactions(currentFilter, limit = 200)
        if (currentResult is AppResult.Error) {
            return currentResult
        }
        val currentTransactions = (currentResult as AppResult.Success).data

        // 2. Ambil transaksi bulan sebelumnya untuk pembanding
        val prevFilter = TransactionFilter(
            startDate = prevStart,
            endDate = prevEnd,
            sortOrder = SortOrder.NEWEST_FIRST
        )
        val prevResult = transactionRepository.getTransactions(prevFilter, limit = 200)
        val prevTransactions = if (prevResult is AppResult.Success) prevResult.data else emptyList()

        // 3. Ambil 5 transaksi terbaru untuk preview
        val recentFilter = TransactionFilter(
            sortOrder = SortOrder.NEWEST_FIRST
        )
        val recentResult = transactionRepository.getTransactions(recentFilter, limit = 5)
        val recentTransactions = if (recentResult is AppResult.Success) recentResult.data else emptyList()

        return AppResult.Success(
            computeSummary(
                currentTransactions = currentTransactions,
                previousTransactions = prevTransactions,
                recentTransactions = recentTransactions,
                referenceDate = referenceDate
            )
        )
    }

    /**
     * Fungsi murni untuk kalkulasi agregasi metrik Dashboard.
     */
    fun computeSummary(
        currentTransactions: List<FuelTransaction>,
        previousTransactions: List<FuelTransaction>,
        recentTransactions: List<FuelTransaction>,
        referenceDate: LocalDate = LocalDate.now()
    ): DashboardSummary {
        val totalExpense = currentTransactions.sumOf { it.totalAmountIdr }
        val count = currentTransactions.size

        var totalVolume = 0.0
        var totalVolumeAmount = 0L
        var hasIncompleteVolume = false

        for (tx in currentTransactions) {
            val vol = tx.volumeLiters
            if (vol != null && vol > 0.0) {
                totalVolume += vol
                totalVolumeAmount += tx.totalAmountIdr
            } else {
                hasIncompleteVolume = true
            }
        }

        val avgExpensePerTx = if (count > 0) totalExpense / count else 0L

        val avgPricePerLiter = if (totalVolume > 0.0 && totalVolumeAmount > 0L) {
            totalVolumeAmount.toDouble() / totalVolume
        } else {
            null
        }

        val prevTotalExpense = previousTransactions.sumOf { it.totalAmountIdr }
        val diffExpense = totalExpense - prevTotalExpense

        val changePercentage = if (prevTotalExpense > 0L) {
            ((totalExpense - prevTotalExpense).toDouble() / prevTotalExpense.toDouble()) * 100.0
        } else {
            null
        }

        // Agregasi per kendaraan
        val vehicleGroup = currentTransactions.groupBy { it.vehicleId }
        val vehicleSummaries = vehicleGroup.map { (vId, txList) ->
            val vName = txList.firstOrNull()?.vehicleName?.takeIf { it.isNotBlank() } ?: "Kendaraan ($vId)"
            val vTotal = txList.sumOf { it.totalAmountIdr }
            val vVol = txList.mapNotNull { it.volumeLiters }.filter { it > 0.0 }.sum()
            val vPct = if (totalExpense > 0L) {
                (vTotal.toDouble() / totalExpense.toDouble()) * 100.0
            } else {
                0.0
            }
            VehicleSpendingSummary(
                vehicleId = vId,
                vehicleName = vName,
                totalAmountIdr = vTotal,
                totalVolumeLiters = vVol,
                transactionCount = txList.size,
                percentageOfTotal = vPct
            )
        }.sortedByDescending { it.totalAmountIdr }

        val topVehicle = vehicleSummaries.firstOrNull()

        return DashboardSummary(
            currentMonth = referenceDate,
            totalExpenseIdr = totalExpense,
            totalVolumeLiters = totalVolume,
            transactionCount = count,
            averageExpensePerTransactionIdr = avgExpensePerTx,
            averagePricePerLiterIdr = avgPricePerLiter,
            previousMonthExpenseIdr = prevTotalExpense,
            expenseDifferenceIdr = diffExpense,
            expenseChangePercentage = changePercentage,
            topSpendingVehicle = topVehicle,
            vehicleSummaries = vehicleSummaries,
            recentTransactions = recentTransactions,
            hasIncompleteVolumeData = hasIncompleteVolume
        )
    }
}
