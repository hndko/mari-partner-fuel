package id.maripartner.fuel.feature.profile.edit

/**
 * State untuk layar Ubah Profil Akun & Kata Sandi.
 */
data class EditProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val timezone: String = "Asia/Jakarta",
    val isGoogleLinked: Boolean = false,
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val displayNameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val showSuccessDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val dialogErrorMessage: String = ""
)

/**
 * Event interaksi pengguna pada layar Edit Profil.
 */
sealed interface EditProfileUiEvent {
    data object LoadProfile : EditProfileUiEvent
    data class DisplayNameChanged(val name: String) : EditProfileUiEvent
    data class TimezoneChanged(val timezone: String) : EditProfileUiEvent
    data class NewPasswordChanged(val password: String) : EditProfileUiEvent
    data class ConfirmPasswordChanged(val password: String) : EditProfileUiEvent
    data object TogglePasswordVisibility : EditProfileUiEvent
    data object ToggleConfirmPasswordVisibility : EditProfileUiEvent
    data object SaveProfile : EditProfileUiEvent
    data object DismissDialog : EditProfileUiEvent
}
