package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodeCheckoutResponseDto_success() {
        val jsonString = """
            {
              "success": true,
              "payment_reference": "MPF-20261029-A1B2C3D4",
              "provider": "google_play",
              "amount_idr": 29000,
              "currency": "IDR",
              "plan_code": "pro_monthly",
              "plan_name": "Mari Pro Bulanan",
              "billing_period": "monthly",
              "product_id": "mari_pro",
              "base_plan_id": "pro-monthly",
              "expires_at": "2026-10-30T12:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<CheckoutResponseDto>(jsonString)
        assertTrue(dto.success)
        assertEquals("MPF-20261029-A1B2C3D4", dto.paymentReference)
        assertEquals("google_play", dto.provider)
        assertEquals(29000L, dto.amountIdr)
        assertEquals("IDR", dto.currency)
        assertEquals("pro_monthly", dto.planCode)
        assertEquals("mari_pro", dto.productId)
        assertEquals("pro-monthly", dto.basePlanId)
        assertNotNull(dto.expiresAt)
    }

    @Test
    fun decodeVerifyPlayPurchaseResponseDto_success() {
        val jsonString = """
            {
              "success": true,
              "is_pro": true,
              "plan_code": "pro_yearly",
              "expires_at": "2027-10-29T12:00:00Z",
              "message": "Langganan Mari Pro berhasil diverifikasi dan diaktifkan."
            }
        """.trimIndent()

        val dto = json.decodeFromString<VerifyPlayPurchaseResponseDto>(jsonString)
        assertTrue(dto.success)
        assertTrue(dto.isPro)
        assertEquals("pro_yearly", dto.planCode)
        assertEquals("2027-10-29T12:00:00Z", dto.expiresAt)
    }

    @Test
    fun decodePaymentStatusResponseDto_success() {
        val jsonString = """
            {
              "success": true,
              "payment_reference": "MPF-20261029-QRIS123",
              "provider": "qrispy",
              "amount_idr": 29000,
              "status": "paid",
              "is_paid": true,
              "is_pro": true,
              "paid_at": "2026-10-29T14:30:00Z",
              "expires_at": "2026-11-29T14:30:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<PaymentStatusResponseDto>(jsonString)
        assertTrue(dto.success)
        assertEquals("MPF-20261029-QRIS123", dto.paymentReference)
        assertEquals("qrispy", dto.provider)
        assertEquals(29000L, dto.amountIdr)
        assertEquals("paid", dto.status)
        assertTrue(dto.isPaid)
        assertTrue(dto.isPro)
    }
}
