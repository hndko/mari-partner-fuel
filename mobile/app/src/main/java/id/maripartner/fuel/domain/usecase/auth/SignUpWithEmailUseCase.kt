package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository

class SignUpWithEmailUseCase(
    private val authRepository: AuthRepository,
    private val validator: ValidateAuthInputUseCase = ValidateAuthInputUseCase()
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AppResult<UserSession> {
        val nameValidation = validator.validateDisplayName(name)
        if (!nameValidation.isSuccess) {
            return AppResult.Error((nameValidation as AppResult.Error).exception)
        }

        val emailValidation = validator.validateEmail(email)
        if (!emailValidation.isSuccess) {
            return AppResult.Error((emailValidation as AppResult.Error).exception)
        }

        val passValidation = validator.validatePassword(password)
        if (!passValidation.isSuccess) {
            return AppResult.Error((passValidation as AppResult.Error).exception)
        }

        val matchValidation = validator.validatePasswordMatch(password, confirmPassword)
        if (!matchValidation.isSuccess) {
            return AppResult.Error((matchValidation as AppResult.Error).exception)
        }

        return authRepository.signUpWithEmail(email, password, name)
    }
}
