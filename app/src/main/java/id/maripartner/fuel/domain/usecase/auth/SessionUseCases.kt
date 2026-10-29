package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentSessionUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): UserSession? {
        return authRepository.getCurrentSession()
    }
}

class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserSession?> {
        return authRepository.observeAuthState()
    }
}
