package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository,
    private val validator: ValidateAuthInputUseCase = ValidateAuthInputUseCase()
) {
    suspend operator fun invoke(email: String): AppResult<Unit> {
        val emailValidation = validator.validateEmail(email)
        if (!emailValidation.isSuccess) {
            return AppResult.Error((emailValidation as AppResult.Error).exception)
        }
        return authRepository.sendPasswordReset(email)
    }
}
