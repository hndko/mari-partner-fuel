package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import java.util.regex.Pattern

/**
 * Validasi ketat format autentikasi di sisi client (Fail-Fast).
 */
class ValidateAuthInputUseCase {

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    )

    fun validateEmail(email: String): AppResult<Unit> {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return AppResult.Error(AppException.Validation("Alamat email tidak boleh kosong."))
        }
        if (!emailPattern.matcher(trimmed).matches()) {
            return AppResult.Error(AppException.Validation("Format alamat email tidak valid."))
        }
        return AppResult.Success(Unit)
    }

    fun validatePassword(password: String): AppResult<Unit> {
        if (password.isBlank()) {
            return AppResult.Error(AppException.Validation("Kata sandi tidak boleh kosong."))
        }
        if (password.length < 6) {
            return AppResult.Error(AppException.Validation("Kata sandi minimal terdiri dari 6 karakter."))
        }
        return AppResult.Success(Unit)
    }

    fun validatePasswordMatch(password: String, confirm: String): AppResult<Unit> {
        if (password != confirm) {
            return AppResult.Error(AppException.Validation("Konfirmasi kata sandi tidak cocok."))
        }
        return AppResult.Success(Unit)
    }

    fun validateDisplayName(name: String): AppResult<Unit> {
        if (name.trim().isBlank()) {
            return AppResult.Error(AppException.Validation("Nama lengkap tidak boleh kosong."))
        }
        if (name.trim().length < 2) {
            return AppResult.Error(AppException.Validation("Nama minimal terdiri dari 2 karakter."))
        }
        return AppResult.Success(Unit)
    }
}
