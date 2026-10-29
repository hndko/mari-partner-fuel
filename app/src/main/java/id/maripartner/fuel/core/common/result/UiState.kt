package id.maripartner.fuel.core.common.result

/**
 * Representasi immutable UI state untuk layar Jetpack Compose (UDF pattern).
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data class Empty(val message: String) : UiState<Nothing>
}
