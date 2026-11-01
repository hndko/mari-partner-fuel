package id.maripartner.fuel.feature.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.LoadProfile -> loadProfile()
            is EditProfileUiEvent.DisplayNameChanged -> {
                _uiState.update {
                    it.copy(
                        displayName = event.name,
                        displayNameError = if (event.name.isBlank()) "Nama lengkap wajib diisi." else null
                    )
                }
            }
            is EditProfileUiEvent.TimezoneChanged -> {
                _uiState.update { it.copy(timezone = event.timezone) }
            }
            is EditProfileUiEvent.NewPasswordChanged -> {
                _uiState.update {
                    val passError = if (event.password.isNotEmpty() && event.password.length < 6) {
                        "Kata sandi baru minimal 6 karakter."
                    } else null
                    val confirmError = if (it.confirmPassword.isNotEmpty() && event.password != it.confirmPassword) {
                        "Konfirmasi kata sandi tidak cocok."
                    } else null
                    it.copy(
                        newPassword = event.password,
                        passwordError = passError,
                        confirmPasswordError = confirmError
                    )
                }
            }
            is EditProfileUiEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    val confirmError = if (event.password != it.newPassword) {
                        "Konfirmasi kata sandi tidak cocok."
                    } else null
                    it.copy(
                        confirmPassword = event.password,
                        confirmPasswordError = confirmError
                    )
                }
            }
            is EditProfileUiEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is EditProfileUiEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            is EditProfileUiEvent.SaveProfile -> saveProfile()
            is EditProfileUiEvent.DismissDialog -> {
                _uiState.update { it.copy(showSuccessDialog = false, showErrorDialog = false) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = authRepository.getCurrentSession()
            val isGoogleLinked = authRepository.isGoogleLinked()
            val email = session?.email.orEmpty()
            var displayName = session?.displayName ?: email.substringBefore("@")
            var timezone = "Asia/Jakarta"

            when (val profileResult = authRepository.getUserProfile()) {
                is AppResult.Success -> {
                    val profile = profileResult.data
                    if (!profile.displayName.isNullOrBlank()) {
                        displayName = profile.displayName.orEmpty()
                    }
                    if (profile.timezone.isNotBlank()) {
                        timezone = profile.timezone
                    }
                }
                is AppResult.Error -> {
                    // Fallback to session metadata
                }
            }

            _uiState.update {
                it.copy(
                    displayName = displayName,
                    email = email,
                    timezone = timezone,
                    isGoogleLinked = isGoogleLinked,
                    isLoading = false
                )
            }
        }
    }

    private fun saveProfile() {
        val current = _uiState.value
        val trimmedName = current.displayName.trim()

        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(displayNameError = "Nama lengkap wajib diisi.") }
            return
        }

        val enteredPassword = current.newPassword
        if (enteredPassword.isNotEmpty()) {
            if (enteredPassword.length < 6) {
                _uiState.update { it.copy(passwordError = "Kata sandi baru minimal 6 karakter.") }
                return
            }
            if (enteredPassword != current.confirmPassword) {
                _uiState.update { it.copy(confirmPasswordError = "Konfirmasi kata sandi tidak cocok.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // 1. Simpan perubahan profil
            val profileRes = authRepository.updateUserProfile(
                displayName = trimmedName,
                themePreference = "system",
                timezone = current.timezone
            )

            if (profileRes is AppResult.Error) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showErrorDialog = true,
                        dialogErrorMessage = profileRes.exception.message ?: "Gagal memperbarui profil."
                    )
                }
                return@launch
            }

            // 2. Jika ada kata sandi baru, perbarui juga di Supabase Auth
            if (enteredPassword.isNotEmpty()) {
                val passRes = authRepository.updatePassword(enteredPassword)
                if (passRes is AppResult.Error) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showErrorDialog = true,
                            dialogErrorMessage = "Profil berhasil disimpan, namun pembaruan kata sandi gagal: ${passRes.exception.message}"
                        )
                    }
                    return@launch
                }
            }

            // Berhasil
            _uiState.update {
                it.copy(
                    isSaving = false,
                    newPassword = "",
                    confirmPassword = "",
                    showSuccessDialog = true
                )
            }
        }
    }
}
