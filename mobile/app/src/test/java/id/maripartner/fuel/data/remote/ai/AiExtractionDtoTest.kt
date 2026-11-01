package id.maripartner.fuel.data.remote.ai

import id.maripartner.fuel.domain.model.ReceiptCandidateData
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AiExtractionDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun jsonParsing_validGeminiResponse_decodesCorrectly() {
        val jsonPayload = """
            {
              "success": true,
              "provider": "gemini_cloud",
              "fields": {
                "transaction_date": "2026-10-29",
                "transaction_time": "14:30",
                "total_amount_idr": 150000,
                "volume_liters": 15.0,
                "price_per_liter_idr": 10000.0,
                "fuel_type": "Pertalite",
                "station_name": "SPBU Pertamina 34.56789",
                "receipt_number": "TRX-00123"
              },
              "warnings": [],
              "needs_review": true
            }
        """.trimIndent()

        val response = json.decodeFromString<AiExtractionResponseDto>(jsonPayload)

        assertTrue(response.success)
        assertEquals("gemini_cloud", response.provider)
        assertNotNull(response.fields)
        assertEquals(150000L, response.fields?.totalAmountIdr)
        assertEquals(15.0, response.fields?.volumeLiters ?: 0.0, 0.001)
        assertEquals("Pertalite", response.fields?.fuelType)
        assertEquals("SPBU Pertamina 34.56789", response.fields?.stationName)
    }

    @Test
    fun fromAiExtraction_mergesWithLocalOcrGracefully() {
        val aiResponse = AiExtractionResponseDto(
            success = true,
            provider = "gemini_cloud",
            fields = AiExtractedFieldsDto(
                transactionDate = "2026-10-29",
                totalAmountIdr = 50000L,
                volumeLiters = null, // AI tidak menemukan volume
                stationName = "SPBU Pertamina Cilandak"
            )
        )

        val localCandidate = ReceiptCandidateData(
            totalAmountIdr = 50000L,
            volumeLiters = 5.0, // Lokal menemukan volume
            fuelType = "Pertalite",
            rawOcrText = "raw text"
        )

        val merged = ReceiptCandidateData.fromAiExtraction(
            aiResponse = aiResponse,
            fallbackRawText = "raw text",
            existingLocal = localCandidate
        )

        assertEquals(50000L, merged.totalAmountIdr)
        assertEquals(5.0, merged.volumeLiters ?: 0.0, 0.001) // Nilai lokal dipertahankan
        assertEquals("Pertalite", merged.fuelType) // Nilai lokal dipertahankan
        assertEquals("SPBU Pertamina Cilandak", merged.stationName) // Nilai AI digunakan
        assertEquals(LocalDate.of(2026, 10, 29), merged.transactionDate)
        assertEquals("gemini_cloud", merged.provider)
    }

    @Test
    fun jsonParsing_unconfiguredSecretError_handlesWithoutCrashing() {
        val jsonPayload = """
            {
              "success": false,
              "provider": "gemini_cloud",
              "fields": null,
              "warnings": ["Secret GEMINI_API_KEY belum dikonfigurasi."],
              "needs_review": true,
              "error": "Layanan Cloud AI belum dikonfigurasi."
            }
        """.trimIndent()

        val response = json.decodeFromString<AiExtractionResponseDto>(jsonPayload)

        assertTrue(!response.success)
        assertNull(response.fields)
        assertEquals("Layanan Cloud AI belum dikonfigurasi.", response.error)
        assertEquals(1, response.warnings.size)
    }
}
