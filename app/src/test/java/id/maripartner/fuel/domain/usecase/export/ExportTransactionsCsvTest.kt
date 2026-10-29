package id.maripartner.fuel.domain.usecase.export

import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.Vehicle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExportTransactionsCsvTest {

    private val useCase = ExportTransactionsCsvUseCase()

    @Test
    fun exportToCsvString_generatesRfc4180CompliantCsvWithBom() {
        val vehicle = Vehicle(id = "v1", name = "Avanza Veloz", make = "Toyota", model = "Veloz", licensePlate = "B 1234 CD")
        val transactions = listOf(
            FuelTransaction(
                id = "tx1",
                vehicleId = "v1",
                vehicleName = "Avanza, Veloz", // mengandung koma
                totalAmountIdr = 250_000L,
                volumeLiters = 20.0,
                pricePerLiterIdr = 12_500.0,
                stationName = "SPBU Pertamina \"34-12345\"", // mengandung kutip ganda
                notes = "Pengisian full tank\nperjalanan dinas", // mengandung newline
                transactionAt = Instant.parse("2026-10-25T14:30:00Z")
            )
        )

        val csv = useCase.exportToCsvString(transactions, mapOf("v1" to vehicle))

        // 1. Verifikasi UTF-8 BOM
        assertTrue("Harus dimulai dengan UTF-8 BOM", csv.startsWith("\uFEFF"))

        // 2. Verifikasi Header
        assertTrue("Harus memuat header Total Pembayaran", csv.contains("Total Pembayaran (Rp)"))
        assertTrue("Harus memuat header Volume (Liter)", csv.contains("Volume (Liter)"))

        // 3. Verifikasi escaping RFC 4180 untuk koma, kutip ganda, dan newline
        assertTrue("Koma dalam nama kendaraan harus di-escape kutip", csv.contains("\"Avanza, Veloz\""))
        assertTrue("Kutip ganda dalam SPBU harus diduplikasi", csv.contains("\"SPBU Pertamina \"\"34-12345\"\"\""))
        assertTrue("Nominal integer Rupiah murni", csv.contains("250000"))
    }

    @Test
    fun exportToCsvString_neutralizesFormulaInjectionThreats() {
        val maliciousTransactions = listOf(
            FuelTransaction(
                id = "tx1",
                vehicleId = "v1",
                totalAmountIdr = 100_000L,
                stationName = "=cmd|' /C calc'!A0", // Formula injeksi berbahaya
                notes = "@SUM(1+1)*cmd", // Formula injeksi Excel
                transactionAt = Instant.now()
            ),
            FuelTransaction(
                id = "tx2",
                vehicleId = "v1",
                totalAmountIdr = 50_000L,
                stationName = "+evil_formula()",
                notes = "-2+3*cmd|' /C calc'!A0",
                transactionAt = Instant.now()
            )
        )

        val csv = useCase.exportToCsvString(maliciousTransactions)

        // Verifikasi karakter formula dinetralkan dengan awalan petik tunggal
        assertTrue("Formula =cmd harus dinetralkan dengan petik tunggal", csv.contains("=\"'=cmd") || csv.contains("''=cmd") || csv.contains("=cmd") && csv.contains("'=cmd"))
        assertTrue("Formula @SUM harus dinetralkan", csv.contains("'@SUM(1+1)*cmd"))
        assertTrue("Formula +evil harus dinetralkan", csv.contains("'+evil_formula()"))
        assertTrue("Formula -2+3*cmd harus dinetralkan", csv.contains("'-2+3*cmd"))
    }

    @Test
    fun isFormulaDangerous_preservesLegitimateNumbers() {
        // Angka murni positif atau negatif yang sah tidak boleh dianggap formula berbahaya
        assertFalse(useCase.isFormulaDangerous("-15"))
        assertFalse(useCase.isFormulaDangerous("-12.5"))
        assertFalse(useCase.isFormulaDangerous("+100"))
        assertFalse(useCase.isFormulaDangerous("Normal text"))

        // Formula berbahaya wajib ditangkap
        assertTrue(useCase.isFormulaDangerous("=1+1"))
        assertTrue(useCase.isFormulaDangerous("@SUM"))
        assertTrue(useCase.isFormulaDangerous("+calc"))
        assertTrue(useCase.isFormulaDangerous("-2+3*cmd"))
        assertTrue(useCase.isFormulaDangerous("\tcmd"))
    }

    @Test
    fun exportToCsvString_excludesInternalTokensAndSignedUrls() {
        val tx = listOf(
            FuelTransaction(
                id = "tx1",
                userId = "user-secret-uuid-12345",
                vehicleId = "v1",
                totalAmountIdr = 50_000L,
                receiptFileId = "file-id-abc",
                signedReceiptUrl = "https://example.supabase.co/storage/v1/object/sign/receipts/user/secret.jpg?token=secret123",
                transactionAt = Instant.now()
            )
        )

        val csv = useCase.exportToCsvString(tx)

        // Verifikasi zero secret / token leakage
        assertFalse("CSV tidak boleh memuat URL signed token rahasia", csv.contains("secret123"))
        assertFalse("CSV tidak boleh memuat userId", csv.contains("user-secret-uuid-12345"))
    }
}
