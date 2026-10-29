package id.maripartner.fuel.domain.usecase.payment

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.PaymentOrder
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.repository.PaymentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PaymentUseCaseTest {

    private class FakePaymentRepository : PaymentRepository {
        override suspend fun createCheckoutOrder(planCode: String, provider: String): AppResult<PaymentOrder> {
            val amount = if (planCode == "pro_yearly") 290000L else 29000L
            return AppResult.Success(
                PaymentOrder(
                    paymentReference = "MPF-TEST-REF",
                    provider = provider,
                    amountIdr = amount,
                    currency = "IDR",
                    planCode = planCode,
                    planName = "Mari Pro Test",
                    billingPeriod = if (planCode == "pro_yearly") "yearly" else "monthly"
                )
            )
        }

        override suspend fun verifyPlayBillingPurchase(
            purchaseToken: String,
            orderId: String,
            productId: String,
            basePlanId: String,
            paymentReference: String?
        ): AppResult<Boolean> {
            return AppResult.Success(purchaseToken.isNotEmpty())
        }

        override suspend fun getPaymentStatus(paymentReference: String): AppResult<PaymentStatus> {
            return AppResult.Success(
                PaymentStatus(
                    paymentReference = paymentReference,
                    status = "paid",
                    amountIdr = 29000L,
                    isPaid = true,
                    isPro = true,
                    paidAt = Instant.now()
                )
            )
        }

        override suspend fun getPaymentHistory(): AppResult<List<PaymentStatus>> {
            return AppResult.Success(emptyList())
        }
    }

    @Test
    fun createCheckoutOrder_returnsValidOrder() = runBlocking {
        val repo = FakePaymentRepository()
        val useCase = CreateCheckoutOrderUseCase(repo)

        val result = useCase("pro_monthly", "google_play")
        assertTrue(result is AppResult.Success)
        val order = (result as AppResult.Success).data
        assertEquals("MPF-TEST-REF", order.paymentReference)
        assertEquals(29000L, order.amountIdr)
        assertEquals("google_play", order.provider)
    }

    @Test
    fun verifyPlayPurchase_returnsTrue() = runBlocking {
        val repo = FakePaymentRepository()
        val useCase = VerifyPlayBillingPurchaseUseCase(repo)

        val result = useCase("token_123", "GPA.123", "mari_pro", "pro-monthly")
        assertTrue(result is AppResult.Success)
        assertTrue((result as AppResult.Success).data)
    }

    @Test
    fun getPaymentStatus_returnsPaidStatus() = runBlocking {
        val repo = FakePaymentRepository()
        val useCase = GetPaymentStatusUseCase(repo)

        val result = useCase("MPF-TEST-REF")
        assertTrue(result is AppResult.Success)
        val status = (result as AppResult.Success).data
        assertTrue(status.isPaid)
        assertTrue(status.isPro)
    }
}
