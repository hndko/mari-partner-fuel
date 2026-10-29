package id.maripartner.fuel.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.maripartner.fuel.core.common.config.AppConfig
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.domain.repository.AuthRepository
import id.maripartner.fuel.domain.usecase.auth.GetCurrentSessionUseCase
import id.maripartner.fuel.domain.usecase.auth.ResetPasswordUseCase
import id.maripartner.fuel.domain.usecase.auth.SignInWithEmailUseCase
import id.maripartner.fuel.domain.usecase.auth.SignInWithGoogleUseCase
import id.maripartner.fuel.domain.usecase.auth.SignOutUseCase
import id.maripartner.fuel.domain.usecase.auth.SignUpWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val signInWithEmailUseCase: SignInWithEmailUseCase = SignInWithEmailUseCase(authRepository),
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase = SignUpWithEmailUseCase(authRepository),
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
    private val resetPasswordUseCase: ResetPasswordUseCase = ResetPasswordUseCase(authRepository),
    private val signOutUseCase: SignOutUseCase = SignOutUseCase(authRepository),
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase = GetCurrentSessionUseCase(authRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            userSession = getCurrentSessionUseCase(),
            isGoogleSignInEnabled = AppConfig.isGoogleAuthAvailable
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.SignIn -> handleSignIn(event.email, event.password)
            is AuthUiEvent.SignUp -> handleSignUp(event.name, event.email, event.password, event.confirmPassword)
            is AuthUiEvent.SignInWithGoogle -> handleSignInWithGoogle(event.idToken)
            is AuthUiEvent.SendPasswordReset -> handlePasswordReset(event.email)
            is AuthUiEvent.SignOut -> handleSignOut()
            is AuthUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            is AuthUiEvent.ClearSuccessMessage -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun handleSignIn(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = signInWithEmailUseCase(email, pass)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userSession = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun handleSignUp(name: String, email: String, pass: String, confirm: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = signUpWithEmailUseCase(name, email, pass, confirm)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userSession = result.data,
                            successMessage = "Pendaftaran berhasil! Silakan periksa email Anda.",
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun handleSignInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = signInWithGoogleUseCase(idToken)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userSession = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun handlePasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = resetPasswordUseCase(email)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordResetSent = true,
                            successMessage = "Tautan pemulihan kata sandi telah dikirim ke email Anda.",
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    private fun handleSignOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            signOutUseCase()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userSession = null,
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }
}
