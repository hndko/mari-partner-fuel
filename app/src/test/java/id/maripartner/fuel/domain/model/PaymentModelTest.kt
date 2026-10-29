package id.maripartner.fuel.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentModelTest {

    @Test
    fun paymentStatus_pendingState_detectedCorrectly() {
        val statusPending = PaymentStatus(
            paymentReference = "MPF-20261029-TEST1",
            status = "pending",
            amountIdr = 29000L,
            isPaid = false,
            isPro = false
        )
        assertTrue(statusPending.isPending)
        assertFalse(statusPending.isPaid)
        assertFalse(statusPending.isFailed)
        assertFalse(statusPending.isExpired)

        val statusCreated = statusPending.copy(status = "created")
        assertTrue(statusCreated.isPending)
    }

    @Test
    fun paymentStatus_paidState_detectedCorrectly() {
        val statusPaid = PaymentStatus(
            paymentReference = "MPF-20261029-TEST2",
            status = "paid",
            amountIdr = 29000L,
            isPaid = true,
            isPro = true,
            paidAt = Instant.now()
        )
        assertFalse(statusPaid.isPending)
        assertTrue(statusPaid.isPaid)
        assertTrue(statusPaid.isPro)
        assertFalse(statusPaid.isFailed)
        assertFalse(statusPaid.isExpired)
    }

    @Test
    fun paymentStatus_expiredState_detectedCorrectly() {
        val pastExpires = Instant.now().minus(10, ChronoUnit.MINUTES)
        val statusExpiredByTime = PaymentStatus(
            paymentReference = "MPF-20261029-TEST3",
            status = "pending",
            amountIdr = 29000L,
            expiresAt = pastExpires
        )
        assertTrue(statusExpiredByTime.isExpired)

        val statusExpiredByStatus = PaymentStatus(
            paymentReference = "MPF-20261029-TEST4",
            status = "expired",
            amountIdr = 29000L
        )
        assertTrue(statusExpiredByStatus.isExpired)
    }

    @Test
    fun paymentStatus_failedState_detectedCorrectly() {
        val statusFailed = PaymentStatus(
            paymentReference = "MPF-20261029-TEST5",
            status = "failed",
            amountIdr = 29000L
        )
        assertTrue(statusFailed.isFailed)

        val statusCancelled = PaymentStatus(
            paymentReference = "MPF-20261029-TEST6",
            status = "cancelled",
            amountIdr = 29000L
        )
        assertTrue(statusCancelled.isFailed)
    }

    @Test
    fun paymentOrder_properties_mappedProperly() {
        val order = PaymentOrder(
            paymentReference = "MPF-20261029-TEST7",
            provider = "google_play",
            amountIdr = 290000L,
            currency = "IDR",
            planCode = "pro_yearly",
            planName = "Mari Pro Tahunan",
            billingPeriod = "yearly",
            productId = "mari_pro",
            basePlanId = "pro-yearly"
        )
        assertEquals("google_play", order.provider)
        assertEquals(290000L, order.amountIdr)
        assertEquals("IDR", order.currency)
        assertEquals("pro_yearly", order.planCode)
        assertEquals("pro-yearly", order.basePlanId)
    }
}
