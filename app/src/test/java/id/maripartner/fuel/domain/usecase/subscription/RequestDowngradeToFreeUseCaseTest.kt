package id.maripartner.fuel.domain.usecase.subscription

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RequestDowngradeToFreeUseCaseTest {

    private class FakeSubscriptionRepo(
        private var entitlement: UserEntitlement
    ) : SubscriptionRepository {
        var downgradeCalled: Boolean = false

        override suspend fun getMyEntitlement(forceRefresh: Boolean): AppResult<UserEntitlement> {
            return AppResult.Success(entitlement)
        }

        override suspend fun getSubscriptionPlans(): AppResult<List<SubscriptionPlan>> {
            return AppResult.Success(emptyList())
        }

        override suspend fun getCachedEntitlement(): UserEntitlement? = entitlement

        override suspend fun requestDowngradeToFree(): AppResult<String> {
            downgradeCalled = true
            entitlement = entitlement.copy(cancelAtPeriodEnd = true, autoRenew = false)
            return AppResult.Success("Downgrade ke paket Free berhasil dicatat")
        }

        override fun clearCache() {}
    }

    @Test
    fun activeProUser_requestDowngrade_maintainsProUntilExpiry() = runBlocking {
        val expiryTime = Instant.now().plusSeconds(86400 * 30) // 30 days remaining
        val initialPro = UserEntitlement(
            userId = "user_123",
            isPro = true,
            planCode = "pro_monthly",
            planName = "Mari Pro Bulanan",
            expiresAt = expiryTime,
            autoRenew = true,
            cancelAtPeriodEnd = false
        )

        val repo = FakeSubscriptionRepo(initialPro)
        val useCase = RequestDowngradeToFreeUseCase(repo)

        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals("Downgrade ke paket Free berhasil dicatat", (result as AppResult.Success).data)
        assertTrue(repo.downgradeCalled)

        // Verifikasi bahwa user tetap Pro dan memiliki cancelAtPeriodEnd = true
        val updatedEntitlement = repo.getMyEntitlement(forceRefresh = true).getOrNull()
        assertTrue(updatedEntitlement != null)
        assertTrue(updatedEntitlement!!.isPro)
        assertTrue(updatedEntitlement.cancelAtPeriodEnd)
        assertEquals(expiryTime, updatedEntitlement.expiresAt)
    }
}
