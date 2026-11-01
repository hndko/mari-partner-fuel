package id.maripartner.fuel.domain.usecase.analytics

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.AnalyticsPeriod
import id.maripartner.fuel.domain.model.AnalyticsReport
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder
import id.maripartner.fuel.domain.model.TimePeriodExpense
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.model.VehicleSpendingSummary
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Use case untuk menyusun laporan analitik pengeluaran BBM, visualisasi time-series,
 * distribusi kendaraan, dan estimasi efisiensi konsumsi (km/L).
 */
class GetAnalyticsReportUseCase(
    private val transactionRepository: FuelTransactionRepository,
    private val calculateFuelEfficiencyUseCase: CalculateFuelEfficiencyUseCase = CalculateFuelEfficiencyUseCase()
) {

    suspend operator fun invoke(
        period: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
        customStartDate: LocalDate? = null,
        customEndDate: LocalDate? = null,
        vehicleId: String? = null,
        today: LocalDate = LocalDate.now()
    ): AppResult<AnalyticsReport> {
        val (startDate, endDate) = resolveDateRange(period, customStartDate, customEndDate, today)

        // 1. Ambil transaksi periode yang dipilih
        val filter = TransactionFilter(
            vehicleId = vehicleId,
            startDate = startDate,
            endDate = endDate,
            sortOrder = SortOrder.OLDEST_FIRST
        )
        val result = transactionRepository.getTransactions(filter, limit = 500)
        if (result is AppResult.Error) {
            return result
        }
        val transactions = (result as AppResult.Success).data

        // 2. Ambil transaksi periode setara sebelumnya untuk pembanding
        val periodDurationDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val prevEndDate = startDate.minusDays(1)
        val prevStartDate = prevEndDate.minusDays(periodDurationDays - 1)

        val prevFilter = TransactionFilter(
            vehicleId = vehicleId,
            startDate = prevStartDate,
            endDate = prevEndDate,
            sortOrder = SortOrder.OLDEST_FIRST
        )
        val prevResult = transactionRepository.getTransactions(prevFilter, limit = 500)
        val prevTransactions = if (prevResult is AppResult.Success) prevResult.data else emptyList()

        return AppResult.Success(
            buildReport(
                period = period,
                startDate = startDate,
                endDate = endDate,
                transactions = transactions,
                previousTransactions = prevTransactions
            )
        )
    }

    /**
     * Menyusun model domain AnalyticsReport murni dari daftar transaksi.
     */
    fun buildReport(
        period: AnalyticsPeriod,
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<FuelTransaction>,
        previousTransactions: List<FuelTransaction> = emptyList()
    ): AnalyticsReport {
        val totalExpense = transactions.sumOf { it.totalAmountIdr }
        val count = transactions.size

        var totalVolume = 0.0
        var totalVolumeAmount = 0L
        var hasIncompleteVolume = false

        for (tx in transactions) {
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
        val changePercentage = if (prevTotalExpense > 0L) {
            ((totalExpense - prevTotalExpense).toDouble() / prevTotalExpense.toDouble()) * 100.0
        } else {
            null
        }

        // Distribusi per kendaraan
        val vehicleGroup = transactions.groupBy { it.vehicleId }
        val vehicleDistribution = vehicleGroup.map { (vId, txList) ->
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

        // Time-series pengeluaran
        val durationDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val timeSeries = if (durationDays <= 31) {
            aggregateDaily(startDate, endDate, transactions)
        } else {
            aggregateMonthly(startDate, endDate, transactions)
        }

        // Estimasi efisiensi BBM km/L
        val efficiencySummaries = calculateFuelEfficiencyUseCase.calculateAll(transactions)

        return AnalyticsReport(
            period = period,
            startDate = startDate,
            endDate = endDate,
            totalExpenseIdr = totalExpense,
            totalVolumeLiters = totalVolume,
            transactionCount = count,
            averageExpensePerTransactionIdr = avgExpensePerTx,
            averagePricePerLiterIdr = avgPricePerLiter,
            previousPeriodExpenseIdr = if (previousTransactions.isNotEmpty()) prevTotalExpense else null,
            periodChangePercentage = changePercentage,
            timeSeriesExpenses = timeSeries,
            vehicleDistribution = vehicleDistribution,
            efficiencySummaries = efficiencySummaries,
            hasIncompleteVolumeData = hasIncompleteVolume
        )
    }

    private fun resolveDateRange(
        period: AnalyticsPeriod,
        customStart: LocalDate?,
        customEnd: LocalDate?,
        today: LocalDate
    ): Pair<LocalDate, LocalDate> {
        return when (period) {
            AnalyticsPeriod.THIS_MONTH -> {
                val ym = YearMonth.from(today)
                ym.atDay(1) to ym.atEndOfMonth()
            }
            AnalyticsPeriod.LAST_MONTH -> {
                val ym = YearMonth.from(today).minusMonths(1)
                ym.atDay(1) to ym.atEndOfMonth()
            }
            AnalyticsPeriod.LAST_3_MONTHS -> {
                val startYm = YearMonth.from(today).minusMonths(2)
                startYm.atDay(1) to YearMonth.from(today).atEndOfMonth()
            }
            AnalyticsPeriod.LAST_6_MONTHS -> {
                val startYm = YearMonth.from(today).minusMonths(5)
                startYm.atDay(1) to YearMonth.from(today).atEndOfMonth()
            }
            AnalyticsPeriod.THIS_YEAR -> {
                LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
            }
            AnalyticsPeriod.CUSTOM -> {
                val start = customStart ?: today.minusDays(30)
                val end = customEnd ?: today
                if (start.isAfter(end)) end to start else start to end
            }
        }
    }

    private fun aggregateDaily(
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<FuelTransaction>
    ): List<TimePeriodExpense> {
        val idLocale = Locale("id", "ID")
        val dayFormatter = DateTimeFormatter.ofPattern("dd MMM", idLocale)
        val zone = java.time.ZoneId.systemDefault()

        val txByDate = transactions.groupBy {
            it.transactionAt.atZone(zone).toLocalDate()
        }

        val result = mutableListOf<TimePeriodExpense>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            val list = txByDate[curr] ?: emptyList()
            val sum = list.sumOf { it.totalAmountIdr }
            val vol = list.mapNotNull { it.volumeLiters }.filter { it > 0.0 }.sum()
            result.add(
                TimePeriodExpense(
                    label = curr.format(dayFormatter),
                    amountIdr = sum,
                    volumeLiters = vol,
                    date = curr
                )
            )
            curr = curr.plusDays(1)
        }
        return result
    }

    private fun aggregateMonthly(
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<FuelTransaction>
    ): List<TimePeriodExpense> {
        val idLocale = Locale("id", "ID")
        val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", idLocale)
        val zone = java.time.ZoneId.systemDefault()

        val txByMonth = transactions.groupBy {
            YearMonth.from(it.transactionAt.atZone(zone).toLocalDate())
        }

        val result = mutableListOf<TimePeriodExpense>()
        var currYm = YearMonth.from(startDate)
        val endYm = YearMonth.from(endDate)

        while (!currYm.isAfter(endYm)) {
            val list = txByMonth[currYm] ?: emptyList()
            val sum = list.sumOf { it.totalAmountIdr }
            val vol = list.mapNotNull { it.volumeLiters }.filter { it > 0.0 }.sum()
            result.add(
                TimePeriodExpense(
                    label = currYm.format(monthFormatter),
                    amountIdr = sum,
                    volumeLiters = vol,
                    date = currYm.atDay(1)
                )
            )
            currYm = currYm.plusMonths(1)
        }
        return result
    }
}
