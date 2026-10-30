package id.maripartner.fuel.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.core.common.result.AppResult
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

            // 2. Muat status langganan
            var isPro = false
            when (val entitlementRes = subscriptionRepository.getMyEntitlement()) {
                is AppResult.Success -> {
                    isPro = entitlementRes.data.isPro
                }
                is AppResult.Error -> { }
            }

            _uiState.update {
                it.copy(
                    userProfile = profileObj,
                    email = email,
                    displayName = displayName.ifBlank { "Pengemudi" },
                    isPro = isPro,
                    isGoogleLinked = isGoogleLinked,
                    themePreference = themePref,
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
}
