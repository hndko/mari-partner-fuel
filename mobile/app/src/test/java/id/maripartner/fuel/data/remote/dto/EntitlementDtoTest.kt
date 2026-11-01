package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodeUserEntitlementDto_success() {
        val jsonString = """
            {
              "user_id": "usr_abc_123",
              "is_pro": true,
              "plan_code": "pro_monthly",
              "plan_name": "Mari Pro Bulanan",
              "status": "active",
              "expires_at": "2026-11-29T12:00:00Z",
              "auto_renew": true,
              "max_vehicles": null,
              "features": ["manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles"],
              "ai_quota": {
                "limit": 100,
                "used": 14,
                "remaining": 86,
                "resets_at": "2026-12-01T00:00:00Z"
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<UserEntitlementDto>(jsonString)
        assertEquals("usr_abc_123", dto.userId)
        assertTrue(dto.isPro)
        assertEquals("pro_monthly", dto.planCode)
        assertEquals("Mari Pro Bulanan", dto.planName)
        assertEquals("active", dto.status)
        assertEquals("2026-11-29T12:00:00Z", dto.expiresAt)
        assertTrue(dto.autoRenew)
        assertNull(dto.maxVehicles)
        assertEquals(4, dto.features.size)
        assertEquals(100, dto.aiQuota.limit)
        assertEquals(14, dto.aiQuota.used)
        assertEquals(86, dto.aiQuota.remaining)
        assertEquals("2026-12-01T00:00:00Z", dto.aiQuota.resetsAt)
    }

    @Test
    fun decodeSubscriptionPlanDto_freeAndPro() {
        val freeJson = """
            {
              "id": "plan_free_uuid",
              "code": "free",
              "name": "Mari Free",
              "billing_period": "free",
              "price_idr": 0,
              "ai_scan_quota": 5,
              "max_vehicles": 2,
              "is_active": true,
              "features": ["manual_transactions", "local_ocr", "export_csv"],
              "description": "Paket esensial gratis"
            }
        """.trimIndent()

        val plan = json.decodeFromString<SubscriptionPlanDto>(freeJson)
        assertEquals("plan_free_uuid", plan.id)
        assertEquals("free", plan.code)
        assertEquals(0L, plan.priceIdr)
        assertEquals(5, plan.aiScanQuota)
        assertEquals(2, plan.maxVehicles)
        assertTrue(plan.isActive)
        val features = plan.getFeaturesList()
        assertEquals(3, features.size)
        assertTrue(features.contains("manual_transactions"))
    }
}
