package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AppResult<UserSession> {
        if (idToken.isBlank()) {
            return AppResult.Error(AppException.Validation("ID Token Google tidak valid."))
        }
        return authRepository.signInWithGoogle(idToken)
    }
}
