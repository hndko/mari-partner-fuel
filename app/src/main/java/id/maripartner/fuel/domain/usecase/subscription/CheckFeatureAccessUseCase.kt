package id.maripartner.fuel.domain.usecase.subscription

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository

class CheckFeatureAccessUseCase(
    private val repository: SubscriptionRepository = SubscriptionRepositoryImpl()
) {
    suspend fun getEntitlement(): UserEntitlement {
        return when (val res = repository.getMyEntitlement(forceRefresh = false)) {
            is AppResult.Success -> res.data
            is AppResult.Error -> UserEntitlement()
        }
    }

    suspend fun canAddVehicle(currentActiveCount: Int): Boolean {
        val ent = getEntitlement()
        return ent.canAddVehicle(currentActiveCount)
    }

    suspend fun canUseCloudAi(): Boolean {
        val ent = getEntitlement()
        return !ent.aiQuota.isExhausted
    }

    suspend fun isPro(): Boolean {
        val ent = getEntitlement()
        return ent.isPro
    }
}
