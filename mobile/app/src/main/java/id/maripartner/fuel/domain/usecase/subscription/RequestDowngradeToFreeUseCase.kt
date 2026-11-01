package id.maripartner.fuel.domain.usecase.subscription

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.domain.repository.SubscriptionRepository

class RequestDowngradeToFreeUseCase(
    private val repository: SubscriptionRepository = SubscriptionRepositoryImpl()
) {
    suspend operator fun invoke(): AppResult<String> {
        return repository.requestDowngradeToFree()
    }
}
