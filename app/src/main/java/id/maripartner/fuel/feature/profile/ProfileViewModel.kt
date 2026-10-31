package id.maripartner.fuel.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.MariPartnerFuelApp
import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.location.SpbuProximityManager
import id.maripartner.fuel.core.security.BiometricAuthManager
import id.maripartner.fuel.core.security.BiometricHardwareStatus
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.data.repository.SubscriptionRepositoryImpl
import id.maripartner.fuel.domain.repository.AuthRepository
import id.maripartner.fuel.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            isGoogleSignInConfigured = AppConfig.isGoogleSignInConfigured
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        onEvent(ProfileUiEvent.LoadProfile)
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfile -> loadProfile()
            is ProfileUiEvent.UpdateDisplayName -> updateDisplayName(event.newName)
            is ProfileUiEvent.LinkGoogle -> linkGoogle(event.idToken)
            is ProfileUiEvent.ToggleReminderNotification -> toggleReminder(event.enabled)
            is ProfileUiEvent.ToggleBiometricLock -> toggleBiometric(event.enabled)
            is ProfileUiEvent.SetLockTimeout -> setLockTimeout(event.minutes)
            is ProfileUiEvent.ToggleSpbuReminder -> toggleSpbuReminder(event.enabled)
            is ProfileUiEvent.UpdateLocationPermission -> _uiState.update { it.copy(hasLocationPermission = event.granted) }
            is ProfileUiEvent.SetEditDialogVisible -> _uiState.update { it.copy(showEditDialog = event.visible) }
            is ProfileUiEvent.SetLogoutDialogVisible -> _uiState.update { it.copy(showLogoutConfirmDialog = event.visible) }
            is ProfileUiEvent.DismissMessage -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = authRepository.getCurrentSession()
            val email = session?.email.orEmpty()
            var displayName = session?.displayName ?: email.substringBefore("@")
            val isGoogleLinked = authRepository.isGoogleLinked()

            // 1. Muat profil dari PostgreSQL
            var profileObj = _uiState.value.userProfile
            var themePref = _uiState.value.themePreference
            when (val profileRes = authRepository.getUserProfile()) {
                is AppResult.Success -> {
                    profileObj = profileRes.data
                    if (!profileRes.data.displayName.isNullOrBlank()) {
                        displayName = profileRes.data.displayName.orEmpty()
                    }
                    themePref = profileRes.data.themePreference
                }
                is AppResult.Error -> {
                    // Fallback ke session jika profil PostgreSQL belum sinkron
                }
            }

            // 2. Muat status langganan terkini (selalu forceRefresh agar mutasi SaaS langsung terdeteksi)
            var isPro = false
            var planName = "Mari Free"
            var expiresStr: String? = null
            when (val entitlementRes = subscriptionRepository.getMyEntitlement(forceRefresh = true)) {
                is AppResult.Success -> {
                    val ent = entitlementRes.data
                    isPro = ent.isPro
                    planName = ent.planName.ifBlank { if (ent.isPro) "Mari Pro" else "Mari Free" }
                    expiresStr = when {
                        ent.isPro && ent.expiresAt == null -> "Permanen (Seumur Hidup)"
                        ent.expiresAt != null -> DateUtils.formatFullDate(ent.expiresAt)
                        else -> null
                    }
                }
                is AppResult.Error -> { }
            }

            val appContext = MariPartnerFuelApp.instance
            val isBioSupported = BiometricAuthManager.canAuthenticate(appContext) == BiometricHardwareStatus.AVAILABLE
            val isBioEnabled = BiometricAuthManager.isBiometricEnabled(appContext)
            val lockTimeout = BiometricAuthManager.getTimeoutMinutes(appContext)
            val isSpbuEnabled = SpbuProximityManager.isSpbuReminderEnabled(appContext)
            val hasLocationPerm = SpbuProximityManager.hasLocationPermission(appContext)

            _uiState.update {
                it.copy(
                    userProfile = profileObj,
                    email = email,
                    displayName = displayName.ifBlank { "Pengemudi" },
                    isPro = isPro,
                    planName = planName,
                    planExpiresAt = expiresStr,
                    isGoogleLinked = isGoogleLinked,
                    themePreference = themePref,
                    isBiometricSupported = isBioSupported,
                    isBiometricEnabled = isBioEnabled,
                    lockTimeoutMinutes = lockTimeout,
                    isSpbuReminderEnabled = isSpbuEnabled,
                    hasLocationPermission = hasLocationPerm,
                    isLoading = false
                )
            }
        }
    }

    private fun updateDisplayName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama lengkap tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }

            when (val result = authRepository.updateUserProfile(trimmed, _uiState.value.themePreference)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            userProfile = result.data,
                            displayName = trimmed,
                            isUpdating = false,
                            showEditDialog = false,
                            successMessage = "Nama profil berhasil diperbarui!"
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = result.exception.message ?: "Gagal memperbarui profil."
                        )
                    }
                }
            }
        }
    }

    private fun linkGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }

            when (val result = authRepository.linkGoogleAccount(idToken)) {
                is AppResult.Success -> {
                    val linked = authRepository.isGoogleLinked()
                    _uiState.update {
                        it.copy(
                            isGoogleLinked = linked,
                            isUpdating = false,
                            successMessage = "Akun Google berhasil ditautkan!"
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = result.exception.message ?: "Gagal menautkan akun Google."
                        )
                    }
                }
            }
        }
    }

    private fun toggleReminder(enabled: Boolean) {
        _uiState.update { it.copy(isReminderNotificationEnabled = enabled) }
    }

    private fun toggleBiometric(enabled: Boolean) {
        val appContext = MariPartnerFuelApp.instance
        BiometricAuthManager.setBiometricEnabled(appContext, enabled)
        _uiState.update {
            it.copy(
                isBiometricEnabled = enabled,
                successMessage = if (enabled) "Kunci Biometrik diaktifkan!" else "Kunci Biometrik dinonaktifkan."
            )
        }
    }

    private fun setLockTimeout(minutes: Int) {
        val appContext = MariPartnerFuelApp.instance
        BiometricAuthManager.setTimeoutMinutes(appContext, minutes)
        _uiState.update {
            it.copy(
                lockTimeoutMinutes = minutes,
                successMessage = "Waktu kunci otomatis diubah."
            )
        }
    }

    private fun toggleSpbuReminder(enabled: Boolean) {
        val appContext = MariPartnerFuelApp.instance
        SpbuProximityManager.setSpbuReminderEnabled(appContext, enabled)
        _uiState.update {
            it.copy(
                isSpbuReminderEnabled = enabled,
                successMessage = if (enabled) "Pengingat SPBU diaktifkan!" else "Pengingat SPBU dinonaktifkan."
            )
        }
    }
}
