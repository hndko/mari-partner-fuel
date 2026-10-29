package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.SubscriptionPlan
import id.maripartner.fuel.domain.model.UserEntitlement

interface SubscriptionRepository {
    suspend fun getMyEntitlement(forceRefresh: Boolean = false): AppResult<UserEntitlement>
    suspend fun getSubscriptionPlans(): AppResult<List<SubscriptionPlan>>
    suspend fun getCachedEntitlement(): UserEntitlement?
}
