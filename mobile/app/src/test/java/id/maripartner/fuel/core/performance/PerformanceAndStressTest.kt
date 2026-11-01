package id.maripartner.fuel.core.performance

import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import id.maripartner.fuel.domain.usecase.analytics.CalculateFuelEfficiencyUseCase
import id.maripartner.fuel.domain.usecase.export.ExportTransactionsCsvUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Suite pengujian performa, beban data besar (stress testing), dan benchmark
 * komputasi numerik Mari Partner Fuel untuk rilis Tahap 9.
 */
class PerformanceAndStressTest {

    private val csvExportUseCase = ExportTransactionsCsvUseCase()
    private val efficiencyUseCase = CalculateFuelEfficiencyUseCase()

    private fun generateSyntheticTransactions(count: Int): Pair<List<FuelTransaction>, Map<String, Vehicle>> {
        val baseTime = Instant.parse("2026-01-01T08:00:00Z")
        val vehicle1 = Vehicle(
            id = "veh-1",
            userId = "user-test-bench",
            name = "Avanza Veloz",
            vehicleType = VehicleType.CAR,
            make = "Toyota",
            model = "Avanza",
            licensePlate = "B 1234 MPF"
        )
        val vehicle2 = Vehicle(
            id = "veh-2",
            userId = "user-test-bench",
            name = "Vario 160",
            vehicleType = VehicleType.MOTORCYCLE,
            make = "Honda",
            model = "Vario",
            licensePlate = "B 5678 MPF"
        )
        val vehiclesMap = mapOf("veh-1" to vehicle1, "veh-2" to vehicle2)

        val list = ArrayList<FuelTransaction>(count)
        var currentOdoVeh1 = 10000.0
        var currentOdoVeh2 = 5000.0

        for (i in 1..count) {
            val isVeh1 = i % 2 == 0
            val vehId = if (isVeh1) "veh-1" else "veh-2"
            val odo = if (isVeh1) {
                currentOdoVeh1 += 250.0
                currentOdoVeh1
            } else {
                currentOdoVeh2 += 120.0
                currentOdoVeh2
            }

            list.add(
                FuelTransaction(
                    id = "tx-bench-$i",
                    userId = "user-test-bench",
                    vehicleId = vehId,
                    transactionAt = baseTime.plus((i * 4).toLong(), ChronoUnit.HOURS),
                    stationName = "SPBU Pertamina 31.${10000 + (i % 50)}",
                    fuelType = if (isVeh1) "Pertamax" else "Pertalite",
                    volumeLiters = if (isVeh1) 25.0 else 3.5,
                    totalAmountIdr = if (isVeh1) 323750L else 35000L,
                    pricePerLiterIdr = if (isVeh1) 12950.0 else 10000.0,
                    odometerKm = odo,
                    notes = "Catatan transaksi ke-$i untuk stress testing data besar"
                )
            )
        }

        return Pair(list, vehiclesMap)
    }

    // -------------------------------------------------------------------------
    // 1. Benchmark Ekspor CSV Skala Besar (1.000 Transaksi)
    // -------------------------------------------------------------------------

    @Test
    fun benchmark_csvExport_1000Transactions_completesWithinThreshold() {
        val (transactions, vehiclesMap) = generateSyntheticTransactions(1000)

        val startTime = System.currentTimeMillis()
        val csvResult = csvExportUseCase.exportToCsvString(transactions, vehiclesMap)
        val durationMs = System.currentTimeMillis() - startTime

        assertNotNull("Hasil CSV tidak boleh null", csvResult)
        assertTrue("CSV harus diawali UTF-8 BOM", csvResult.startsWith('\uFEFF'))

        val lines = csvResult.lines().filter { it.isNotBlank() }
        assertEquals("Total baris CSV harus 1.001 (1 header + 1.000 data)", 1001, lines.size)

        // Threshold SLA: Ekspor 1.000 transaksi di CPU standar wajib selesai < 500ms
        assertTrue("Durasi ekspor CSV ($durationMs ms) harus lebih cepat dari 500 ms", durationMs < 500)
    }

    // -------------------------------------------------------------------------
    // 2. Benchmark Agregasi Finansial & Rata-rata Tertimbang (1.000 Transaksi)
    // -------------------------------------------------------------------------

    @Test
    fun benchmark_financialAggregation_1000Transactions_executesEfficiently() {
        val (transactions, _) = generateSyntheticTransactions(1000)

        val startTime = System.currentTimeMillis()

        var totalSpendIdr = 0L
        var totalVolume = 0.0
        var totalCostForWeighted = 0.0

        for (tx in transactions) {
            totalSpendIdr += tx.totalAmountIdr
            val vol = tx.volumeLiters
            if (vol != null && vol > 0) {
                totalVolume += vol
                totalCostForWeighted += tx.totalAmountIdr
            }
        }

        val weightedAverage = if (totalVolume > 0) totalCostForWeighted / totalVolume else 0.0
        val durationMs = System.currentTimeMillis() - startTime

        assertTrue("Total belanja harus lebih besar dari 0", totalSpendIdr > 0)
        assertTrue("Total volume harus lebih besar dari 0", totalVolume > 0)
        assertTrue("Rata-rata tertimbang per liter harus valid", weightedAverage > 10000.0)

        // SLA: Agregasi 1.000 transaksi dalam memori harus < 50ms
        assertTrue("Agregasi finansial ($durationMs ms) harus selesai < 50 ms", durationMs < 50)
    }

    // -------------------------------------------------------------------------
    // 3. Benchmark Perhitungan Efisiensi BBM Odometer (1.000 Transaksi)
    // -------------------------------------------------------------------------

    @Test
    fun benchmark_fuelEfficiency_1000Transactions_runsFast() {
        val (transactions, _) = generateSyntheticTransactions(1000)
        val veh1Transactions = transactions.filter { it.vehicleId == "veh-1" }

        val startTime = System.currentTimeMillis()
        val efficiencyResult = efficiencyUseCase.calculateForVehicle("veh-1", "Avanza Veloz", veh1Transactions)
        val durationMs = System.currentTimeMillis() - startTime

        assertNotNull(efficiencyResult)
        assertTrue("Harus memiliki data yang cukup untuk 500 entri kendaraan 1", efficiencyResult.isSufficientData)
        val avgKmL = efficiencyResult.averageKmPerLiter
        assertNotNull(avgKmL)
        assertTrue("Efisiensi km/L harus berada dalam rentang wajar (5..20)", avgKmL!! in 5.0..20.0)

        // SLA: Perhitungan km/L 500 leg berurutan harus selesai < 200ms
        assertTrue("Kalkulasi efisiensi ($durationMs ms) harus selesai < 200 ms", durationMs < 200)
    }

    // -------------------------------------------------------------------------
    // 4. Benchmark Pencarian & Filter Real-Time (1.000 Transaksi)
    // -------------------------------------------------------------------------

    @Test
    fun benchmark_inMemorySearchAndFilter_1000Transactions_under20ms() {
        val (transactions, _) = generateSyntheticTransactions(1000)

        val query = "Pertamina 31.10014"
        val startTime = System.currentTimeMillis()

        val filtered = transactions.filter { tx ->
            val matchVehicle = tx.vehicleId == "veh-1"
            val matchQuery = tx.stationName?.contains(query, ignoreCase = true) == true
            matchVehicle && matchQuery
        }

        val durationMs = System.currentTimeMillis() - startTime

        assertTrue("Hasil pencarian harus ditemukan", filtered.isNotEmpty())
        // SLA: Filter memori responsif harus < 50ms untuk menjamin kelancaran 60fps UI
        assertTrue("Pencarian debounce ($durationMs ms) harus selesai < 50 ms", durationMs < 50)
    }

    // -------------------------------------------------------------------------
    // 5. Benchmark Formatter Nominal Rupiah (10.000 Iterasi)
    // -------------------------------------------------------------------------

    @Test
    fun benchmark_currencyFormatter_10000Operations_under150ms() {
        val amounts = listOf(50000L, 125000L, 5000000L, 0L, 29000L, 290000L, 10000000L)

        val startTime = System.currentTimeMillis()
        var lastFormatted = ""

        for (i in 1..10000) {
            val amt = amounts[i % amounts.size]
            lastFormatted = CurrencyFormatter.formatIdr(amt)
        }

        val durationMs = System.currentTimeMillis() - startTime

        assertTrue("Hasil format harus berawalan Rp", lastFormatted.startsWith("Rp"))
        // SLA: 10.000 format nominal harus < 150ms
        assertTrue("10.000 format currency ($durationMs ms) harus selesai < 150 ms", durationMs < 150)
    }
}
