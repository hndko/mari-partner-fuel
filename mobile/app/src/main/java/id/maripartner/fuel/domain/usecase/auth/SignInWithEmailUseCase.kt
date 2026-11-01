package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository

class SignInWithEmailUseCase(
    private val authRepository: AuthRepository,
    private val validator: ValidateAuthInputUseCase = ValidateAuthInputUseCase()
) {
    suspend operator fun invoke(email: String, password: String): AppResult<UserSession> {
        val emailValidation = validator.validateEmail(email)
        if (!emailValidation.isSuccess) {
            return AppResult.Error((emailValidation as AppResult.Error).exception)
        }

        val passValidation = validator.validatePassword(password)
        if (!passValidation.isSuccess) {
            return AppResult.Error((passValidation as AppResult.Error).exception)
        }

        return authRepository.signInWithEmail(email, password)
    }
}
