package id.maripartner.fuel.domain.usecase.subscription

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.AiQuotaStatus
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckFeatureAccessUseCaseTest {

    private class FakeSubscriptionRepository(
        private val entitlement: UserEntitlement
    ) : SubscriptionRepository {
        override suspend fun getMyEntitlement(forceRefresh: Boolean): AppResult<UserEntitlement> {
            return AppResult.Success(entitlement)
        }

        override suspend fun getSubscriptionPlans(): AppResult<List<SubscriptionPlan>> {
            return AppResult.Success(emptyList())
        }

        override suspend fun getCachedEntitlement(): UserEntitlement? = entitlement
        override fun clearCache() {}
    }

    @Test
    fun freeUser_cannotExceedTwoVehicles() = runBlocking {
        val freeEntitlement = UserEntitlement(
            userId = "u1",
            isPro = false,
            planCode = "free",
            maxVehicles = 2,
            aiQuota = AiQuotaStatus(limit = 5, used = 1, remaining = 4)
        )
        val repo = FakeSubscriptionRepository(freeEntitlement)
        val useCase = CheckFeatureAccessUseCase(repo)

        assertFalse(useCase.isPro())
        assertTrue(useCase.canAddVehicle(0))
        assertTrue(useCase.canAddVehicle(1))
        assertFalse(useCase.canAddVehicle(2))
        assertFalse(useCase.canAddVehicle(3))
        assertTrue(useCase.canUseCloudAi())
    }

    @Test
    fun freeUser_quotaExhausted_cannotUseCloudAi() = runBlocking {
        val exhaustedEntitlement = UserEntitlement(
            userId = "u2",
            isPro = false,
            planCode = "free",
            maxVehicles = 2,
            aiQuota = AiQuotaStatus(limit = 5, used = 5, remaining = 0)
        )
        val repo = FakeSubscriptionRepository(exhaustedEntitlement)
        val useCase = CheckFeatureAccessUseCase(repo)

        assertFalse(useCase.canUseCloudAi())
    }

    @Test
    fun proUser_unlimitedVehiclesAndCloudAi() = runBlocking {
        val proEntitlement = UserEntitlement(
            userId = "u3",
            isPro = true,
            planCode = "pro_monthly",
            maxVehicles = null,
            aiQuota = AiQuotaStatus(limit = 100, used = 20, remaining = 80)
        )
        val repo = FakeSubscriptionRepository(proEntitlement)
        val useCase = CheckFeatureAccessUseCase(repo)

        assertTrue(useCase.isPro())
        assertTrue(useCase.canAddVehicle(10))
        assertTrue(useCase.canUseCloudAi())
    }
}
