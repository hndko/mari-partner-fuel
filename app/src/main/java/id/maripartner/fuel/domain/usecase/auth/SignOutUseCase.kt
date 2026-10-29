package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return authRepository.signOut()
    }
}
