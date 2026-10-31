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
    val planName: String = "Mari Free",
    val planExpiresAt: String? = null,
    val isGoogleLinked: Boolean = false,
    val isGoogleSignInConfigured: Boolean = false,
    val themePreference: String = "system",
    val isReminderNotificationEnabled: Boolean = true,
    val isBiometricSupported: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val lockTimeoutMinutes: Int = 0,
    val isSpbuReminderEnabled: Boolean = true,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showEditDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false
) {
    val isOwnerOrAdmin: Boolean
        get() = userProfile?.isOwnerOrAdmin == true || email.equals("kyuoko13@gmail.com", ignoreCase = true)
}

/**
 * Event interaksi pengguna pada layar Profil.
 */
sealed interface ProfileUiEvent {
    data object LoadProfile : ProfileUiEvent
    data class UpdateDisplayName(val newName: String) : ProfileUiEvent
    data class LinkGoogle(val idToken: String) : ProfileUiEvent
    data class ToggleReminderNotification(val enabled: Boolean) : ProfileUiEvent
    data class ToggleBiometricLock(val enabled: Boolean) : ProfileUiEvent
    data class SetLockTimeout(val minutes: Int) : ProfileUiEvent
    data class ToggleSpbuReminder(val enabled: Boolean) : ProfileUiEvent
    data class UpdateLocationPermission(val granted: Boolean) : ProfileUiEvent
    data class SetEditDialogVisible(val visible: Boolean) : ProfileUiEvent
    data class SetLogoutDialogVisible(val visible: Boolean) : ProfileUiEvent
    data object DismissMessage : ProfileUiEvent
}
