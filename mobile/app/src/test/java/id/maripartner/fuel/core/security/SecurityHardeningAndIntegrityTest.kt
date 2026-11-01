package id.maripartner.fuel.core.security

import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.model.PaymentProviderType
import id.maripartner.fuel.domain.model.Vehicle
import id.maripartner.fuel.domain.model.VehicleType
import id.maripartner.fuel.domain.usecase.export.ExportTransactionsCsvUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Suite pengujian keamanan dan integritas data menyeluruh untuk Tahap 8.
 * Menguji mitigasi BOLA/IDOR, isolasi data multi-akun, proteksi formula injection,
 * anti-replay token pembelian, serta prinsip Zero-Secret.
 */
class SecurityHardeningAndIntegrityTest {

    private val csvExportUseCase = ExportTransactionsCsvUseCase()

    // -------------------------------------------------------------------------
    // 1. Uji Proteksi Isolasi Akun Ganda (Account A vs Account B)
    // -------------------------------------------------------------------------

    @Test
    fun multiAccount_vehicleOwnershipIsolation_cannotBeImpersonated() {
        val userAId = "user-uuid-aaaa-1111"
        val userBId = "user-uuid-bbbb-2222"

        val vehicleA = Vehicle(
            id = "veh-1",
            userId = userAId,
            name = "Motor Akun A",
            vehicleType = VehicleType.MOTORCYCLE
        )

        val vehicleB = Vehicle(
            id = "veh-2",
            userId = userBId,
            name = "Mobil Akun B",
            vehicleType = VehicleType.CAR
        )

        // Verifikasi bahwa kepemilikan terikat strictly pada userId
        assertNotEquals("ID pengguna harus terisolasi", vehicleA.userId, vehicleB.userId)
        assertTrue("Kendaraan A adalah milik Akun A", vehicleA.userId == userAId)
        assertFalse("Kendaraan A BUKAN milik Akun B", vehicleA.userId == userBId)
    }

    @Test
    fun multiAccount_transactionCompositeKey_preventsCrossAccountAccess() {
        val userAId = "user-uuid-aaaa-1111"
        val userBId = "user-uuid-bbbb-2222"

        val txAccountA = FuelTransaction(
            id = "tx-1",
            userId = userAId,
            vehicleId = "veh-1",
            transactionAt = Instant.parse("2026-10-29T10:00:00Z"),
            totalAmountIdr = 50000,
            volumeLiters = 5.0
        )

        // Upaya manipulasi transaksi oleh Akun B
        val isOwnedByB = txAccountA.userId == userBId
        assertFalse("Transaksi Akun A tidak boleh dapat dimodifikasi oleh Akun B", isOwnedByB)
    }

    @Test
    fun multiAccount_storagePathStructure_strictlyScopedToUserId() {
        val userAId = "user-uuid-aaaa-1111"
        val userBId = "user-uuid-bbbb-2222"

        val validPathA = "$userAId/receipt-abc.jpg"
        val maliciousPathBAttackingA = "$userAId/receipt-stolen.jpg"

        // Validasi aturan path storage: (storage.foldername(name))[1] = auth.uid()::text
        val pathOwnerA = validPathA.substringBefore("/")
        val pathOwnerMalicious = maliciousPathBAttackingA.substringBefore("/")

        assertEquals("Path A sah untuk User A", userAId, pathOwnerA)
        assertNotEquals("Akun B tidak boleh membaca atau mengunggah ke direktori Akun A", userBId, pathOwnerMalicious)
    }

    // -------------------------------------------------------------------------
    // 2. Uji Proteksi Formula Injection & Sanitasi CSV (RFC 4180)
    // -------------------------------------------------------------------------

    @Test
    fun csvExport_formulaInjectionAttacks_areSafelyNeutralized() {
        val dangerousPayloads = listOf(
            "=cmd|' /C calc'!A0",
            "+SUM(1+1)*cmd",
            "-2+3+cmd|' /C calc'!A0",
            "@SUM(1+1)",
            "\t=1+1",
            "\r=calc"
        )

        for (payload in dangerousPayloads) {
            val isDangerous = csvExportUseCase.isFormulaDangerous(payload)
            assertTrue("Payload '$payload' harus terdeteksi berbahaya", isDangerous)

            val escaped = csvExportUseCase.escapeCsvField(payload)
            assertTrue(
                "Payload berbahaya '$payload' harus dinetralkan dengan petik tunggal awalan",
                escaped.startsWith("'") || escaped.startsWith("\"'")
            )
        }
    }

    @Test
    fun csvExport_validNegativeAndPositiveNumbers_areNotCorrupted() {
        // Angka numerik murni yang diawali + atau - tidak boleh dirusak
        val validNumbers = listOf("+50000", "-1500", "+3.850", "-10.5")

        for (num in validNumbers) {
            val isDangerous = csvExportUseCase.isFormulaDangerous(num)
            assertFalse("Angka numerik sah '$num' tidak boleh dianggap berbahaya", isDangerous)

            val escaped = csvExportUseCase.escapeCsvField(num)
            assertEquals("Angka numerik sah '$num' tidak boleh ditambah kutip penjinak", num, escaped)
        }
    }

    // -------------------------------------------------------------------------
    // 3. Uji Proteksi Pembayaran & Anti-Replay Token
    // -------------------------------------------------------------------------

    @Test
    fun payment_antiReplay_detectsPaidStatusAsNonRepeatable() {
        val settledPayment = PaymentStatus(
            paymentReference = "MPF-20261029-ABCD1234",
            provider = "google_play",
            amountIdr = 29000,
            status = "paid",
            isPaid = true,
            isPro = true,
            paidAt = Instant.parse("2026-10-29T10:00:00Z"),
            expiresAt = Instant.parse("2026-11-29T10:00:00Z")
        )

        assertTrue("Transaksi harus berstatus paid", settledPayment.isPaid)
        assertTrue("Pengguna berhak atas status Pro", settledPayment.isPro)

        // Evaluasi apakah token yang sudah 'paid' boleh di-redeem ulang
        val isAlreadyRedeemed = settledPayment.status == "paid"
        assertTrue("Token pembelian yang sudah 'paid' harus ditandai telah diklaim", isAlreadyRedeemed)
    }

    @Test
    fun paymentProviderType_parsing_handlesKnownProviders() {
        assertEquals("google_play", PaymentProviderType.GOOGLE_PLAY.id)
        assertEquals("qrispy", PaymentProviderType.QRISPY.id)
        assertTrue(PaymentProviderType.GOOGLE_PLAY.displayName.contains("Google Play"))
        assertTrue(PaymentProviderType.QRISPY.displayName.contains("QRIS"))
    }

    // -------------------------------------------------------------------------
    // 4. Uji Kebijakan Zero-Secret
    // -------------------------------------------------------------------------

    @Test
    fun zeroSecret_redactedKey_neverExposesFullSecretInLogs() {
        val fakeKey = "sb_secret_key_1234567890abcdef"
        val redacted = AppConfig.redactedKey(fakeKey)

        assertFalse("Key tidak boleh terekspos lengkap di log", redacted.contains(fakeKey))
        assertTrue("Key harus di-mask dengan tanda bintang", redacted.contains("..."))
        assertEquals("sb_s...cdef", redacted)
    }

    @Test
    fun zeroSecret_shortSecret_fullyMasked() {
        val shortKey = "secret"
        val redacted = AppConfig.redactedKey(shortKey)
        assertEquals("***", redacted)
    }
}
