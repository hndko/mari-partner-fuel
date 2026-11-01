package id.maripartner.fuel.core.common.result

/**
 * Result wrapper terstandar untuk layer domain dan repository.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val exception: AppException) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data
}

sealed class AppException(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String = "Gagal terhubung ke server", cause: Throwable? = null) : AppException(message, cause)
    class Authentication(message: String = "Sesi telah berakhir atau tidak valid", cause: Throwable? = null) : AppException(message, cause)
    class Validation(message: String) : AppException(message)
    class NotFound(message: String = "Data tidak ditemukan") : AppException(message)
    class QuotaExceeded(message: String = "Batas kuota telah tercapai") : AppException(message)
    class Unknown(message: String = "Terjadi kesalahan yang tidak terduga", cause: Throwable? = null) : AppException(message, cause)
}
