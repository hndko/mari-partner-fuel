package id.maripartner.fuel.feature.auth

import id.maripartner.fuel.domain.model.UserSession

/**
 * State UI untuk fitur autentikasi.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val userSession: UserSession? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isPasswordResetSent: Boolean = false,
    val isGoogleSignInEnabled: Boolean = false
)

/**
 * Event UI yang dipancarkan Composable ke AuthViewModel.
 */
sealed interface AuthUiEvent {
    data class SignIn(val email: String, val password: String) : AuthUiEvent
    data class SignUp(val name: String, val email: String, val password: String, val confirmPassword: String) : AuthUiEvent
    data class SignInWithGoogle(val idToken: String) : AuthUiEvent
    data class SendPasswordReset(val email: String) : AuthUiEvent
    data object SignOut : AuthUiEvent
    data object DismissError : AuthUiEvent
    data object ClearSuccessMessage : AuthUiEvent
}
