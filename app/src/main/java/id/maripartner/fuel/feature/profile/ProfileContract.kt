package id.maripartner.fuel.feature.profile

import id.maripartner.fuel.domain.model.UserProfile

/**
 * State untuk layar Profil & Pengaturan Mari Partner Fuel.
 */
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val email: String = "",
    val displayName: String = "Pengemudi",
    val isPro: Boolean = false,
    val isGoogleLinked: Boolean = false,
    val isGoogleSignInConfigured: Boolean = false,
    val themePreference: String = "system",
    val isReminderNotificationEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showEditDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false
)

/**
 * Event interaksi pengguna pada layar Profil.
 */
sealed interface ProfileUiEvent {
    data object LoadProfile : ProfileUiEvent
    data class UpdateDisplayName(val newName: String) : ProfileUiEvent
    data class LinkGoogle(val idToken: String) : ProfileUiEvent
    data class ToggleReminderNotification(val enabled: Boolean) : ProfileUiEvent
    data class SetEditDialogVisible(val visible: Boolean) : ProfileUiEvent
    data class SetLogoutDialogVisible(val visible: Boolean) : ProfileUiEvent
    data object DismissMessage : ProfileUiEvent
}
