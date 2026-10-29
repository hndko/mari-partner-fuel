package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.domain.model.FuelTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FuelTransactionDtoMappingTest {

    @Test
    fun toDomain_mapsWithJoinedVehicleAndReceipt() {
        val vehicleDto = VehicleDto(
            id = "veh-1",
            name = "Vario 160",
            vehicleType = "motorcycle",
            licensePlate = "B 1234 ABC"
        )
        val receiptDto = ReceiptFileDto(
            id = "rec-1",
            storagePath = "user-1/receipt.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024L
        )
        val dto = FuelTransactionDto(
            id = "tx-1",
            userId = "user-1",
            vehicleId = "veh-1",
            receiptFileId = "rec-1",
            transactionAt = "2026-10-29T10:00:00Z",
            stationName = "SPBU Pertamina 31.xxx",
            fuelType = "Pertamax",
            volumeLiters = 3.5,
            totalAmountIdr = 45000L,
            pricePerLiterIdr = 12850.0,
            odometerKm = 12500.0,
            scanStatus = "manual",
            notes = "Isi bensin pagi",
            createdAt = "2026-10-29T10:05:00Z",
            updatedAt = "2026-10-29T10:05:00Z",
            vehicles = vehicleDto,
            receiptFiles = receiptDto
        )

        val domain = dto.toDomain(signedReceiptUrl = "https://supabase.co/signed-url-sample")

        assertEquals("tx-1", domain.id)
        assertEquals("user-1", domain.userId)
        assertEquals("veh-1", domain.vehicleId)
        assertEquals("Vario 160", domain.vehicleName)
        assertEquals("B 1234 ABC", domain.vehiclePlate)
        assertEquals("rec-1", domain.receiptFileId)
        assertEquals("user-1/receipt.jpg", domain.storagePath)
        assertEquals("https://supabase.co/signed-url-sample", domain.signedReceiptUrl)
        assertTrue(domain.hasReceipt)
        assertEquals(45000L, domain.totalAmountIdr)
        assertEquals(3.5, domain.volumeLiters ?: 0.0, 0.001)
        assertEquals("SPBU Pertamina 31.xxx", domain.stationName)
        assertEquals("Pertamax", domain.fuelType)
    }

    @Test
    fun fromDomain_omitsEmptyIds() {
        val tx = FuelTransaction(
            vehicleId = "veh-1",
            totalAmountIdr = 50000L,
            volumeLiters = null,
            transactionAt = Instant.parse("2026-10-29T12:00:00Z")
        )

        val dto = FuelTransactionDto.fromDomain(tx, userId = "user-active")

        assertNull(dto.id)
        assertEquals("user-active", dto.userId)
        assertEquals("veh-1", dto.vehicleId)
        assertNull(dto.volumeLiters)
        assertEquals(50000L, dto.totalAmountIdr)
        assertEquals("2026-10-29T12:00:00Z", dto.transactionAt)
    }
}
