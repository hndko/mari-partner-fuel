package id.maripartner.fuel.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class UserEntitlementTest {

    @Test
    fun freeUser_hasMaxTwoVehiclesLimit() {
        val entitlement = UserEntitlement(
            userId = "user_free_1",
            isPro = false,
            planCode = "free",
            planName = "Mari Free",
            status = "active",
            maxVehicles = 2,
            features = listOf("manual_transactions", "local_ocr", "export_csv", "vehicles_limit_2", "ai_scans_5"),
            aiQuota = AiQuotaStatus(limit = 5, used = 2, remaining = 3)
        )

        assertFalse(entitlement.isPro)
        assertTrue(entitlement.canAddVehicle(0))
        assertTrue(entitlement.canAddVehicle(1))
        assertFalse(entitlement.canAddVehicle(2))
        assertFalse(entitlement.canAddVehicle(3))
        assertEquals(3, entitlement.aiQuota.remaining)
        assertFalse(entitlement.aiQuota.isExhausted)
    }

    @Test
    fun proUser_hasUnlimitedVehicles() {
        val entitlement = UserEntitlement(
            userId = "user_pro_1",
            isPro = true,
            planCode = "pro_monthly",
            planName = "Mari Pro Bulanan",
            status = "active",
            maxVehicles = null,
            features = listOf("manual_transactions", "local_ocr", "cloud_ai_extraction", "unlimited_vehicles", "ai_scans_100"),
            aiQuota = AiQuotaStatus(limit = 100, used = 10, remaining = 90)
        )

        assertTrue(entitlement.isPro)
        assertTrue(entitlement.canAddVehicle(0))
        assertTrue(entitlement.canAddVehicle(2))
        assertTrue(entitlement.canAddVehicle(100))
        assertEquals(90, entitlement.aiQuota.remaining)
        assertFalse(entitlement.aiQuota.isExhausted)
    }

    @Test
    fun expiredSubscription_isDetectedCorrectly() {
        val pastInstant = Instant.now().minus(2, ChronoUnit.DAYS)
        val entitlement = UserEntitlement(
            userId = "user_expired_1",
            isPro = true,
            planCode = "pro_monthly",
            status = "expired",
            expiresAt = pastInstant
        )

        assertTrue(entitlement.isExpired)
    }

    @Test
    fun gracePeriodSubscription_isDetectedCorrectly() {
        val futureInstant = Instant.now().plus(1, ChronoUnit.DAYS)
        val entitlement = UserEntitlement(
            userId = "user_grace_1",
            isPro = true,
            planCode = "pro_monthly",
            status = "grace_period",
            expiresAt = futureInstant
        )

        assertTrue(entitlement.isGracePeriod)
        assertFalse(entitlement.isExpired)
    }

    @Test
    fun aiQuotaStatus_exhaustedCalculation() {
        val quotaFull = AiQuotaStatus(limit = 5, used = 5, remaining = 0)
        assertTrue(quotaFull.isExhausted)
        assertEquals(1.0f, quotaFull.usagePercent, 0.01f)

        val quotaPartial = AiQuotaStatus(limit = 100, used = 25, remaining = 75)
        assertFalse(quotaPartial.isExhausted)
        assertEquals(0.25f, quotaPartial.usagePercent, 0.01f)
    }
}
