package id.maripartner.fuel.domain.usecase.subscription

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.domain.model.UserEntitlement
import id.maripartner.fuel.domain.repository.SubscriptionRepository

class GetMyEntitlementsUseCase(
    private val repository: SubscriptionRepository = SubscriptionRepositoryImpl()
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): AppResult<UserEntitlement> {
        return repository.getMyEntitlement(forceRefresh)
    }
}
