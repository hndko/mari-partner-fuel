package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.UserProfile
import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    var activeSession: UserSession? = null
    var shouldFail: Boolean = false
    val authStateFlow = MutableStateFlow<UserSession?>(null)

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): AppResult<UserSession> {
        if (shouldFail) return AppResult.Error(AppException.Validation("Alamat email sudah terdaftar."))
        val session = UserSession(userId = "new-user-123", email = email, displayName = displayName)
        activeSession = session
        authStateFlow.value = session
        return AppResult.Success(session)
    }

    override suspend fun signInWithEmail(email: String, password: String): AppResult<UserSession> {
        if (shouldFail) return AppResult.Error(AppException.Authentication("Email atau kata sandi tidak valid"))
        val session = UserSession(userId = "existing-user-123", email = email, displayName = "Driver")
        activeSession = session
        authStateFlow.value = session
        return AppResult.Success(session)
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<UserSession> {
        if (shouldFail) return AppResult.Error(AppException.Authentication("Token Google kadaluwarsa"))
        val session = UserSession(userId = "google-user-123", email = "google@gmail.com", displayName = "Google User")
        activeSession = session
        authStateFlow.value = session
        return AppResult.Success(session)
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> {
        if (shouldFail) return AppResult.Error(AppException.NotFound("Email tidak terdaftar"))
        return AppResult.Success(Unit)
    }

    override suspend fun signOut(): AppResult<Unit> {
        activeSession = null
        authStateFlow.value = null
        return AppResult.Success(Unit)
    }

    override fun getCurrentSession(): UserSession? = activeSession

    override fun observeAuthState(): Flow<UserSession?> = authStateFlow

    override suspend fun getUserProfile(): AppResult<UserProfile> {
        val s = activeSession ?: return AppResult.Error(AppException.Authentication())
        return AppResult.Success(UserProfile(id = s.userId, displayName = s.displayName))
    }

    override suspend fun updateUserProfile(displayName: String, themePreference: String, timezone: String): AppResult<UserProfile> {
        val s = activeSession ?: return AppResult.Error(AppException.Authentication())
        return AppResult.Success(UserProfile(id = s.userId, displayName = displayName, themePreference = themePreference, timezone = timezone))
    }

    override suspend fun updatePassword(newPassword: String): AppResult<Unit> {
        if (shouldFail) return AppResult.Error(AppException.Validation("Gagal mengubah kata sandi"))
        return AppResult.Success(Unit)
    }

    override suspend fun linkGoogleAccount(idToken: String): AppResult<Unit> {
        if (shouldFail) return AppResult.Error(AppException.Validation("Gagal menautkan akun Google"))
        return AppResult.Success(Unit)
    }

    override fun isGoogleLinked(): Boolean = false
}

class AuthUseCaseTest {

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var signInUseCase: SignInWithEmailUseCase
    private lateinit var signUpUseCase: SignUpWithEmailUseCase
    private lateinit var resetPassUseCase: ResetPasswordUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var googleSignInUseCase: SignInWithGoogleUseCase

    @Before
    fun setup() {
        fakeRepo = FakeAuthRepository()
        signInUseCase = SignInWithEmailUseCase(fakeRepo)
        signUpUseCase = SignUpWithEmailUseCase(fakeRepo)
        resetPassUseCase = ResetPasswordUseCase(fakeRepo)
        signOutUseCase = SignOutUseCase(fakeRepo)
        googleSignInUseCase = SignInWithGoogleUseCase(fakeRepo)
    }

    @Test
    fun signInWithEmail_succeedsOnValidInput() = runBlocking {
        val result = signInUseCase("user@test.com", "password123")
        assertTrue(result.isSuccess)
        assertEquals("existing-user-123", (result as AppResult.Success).data.userId)
        assertEquals("existing-user-123", fakeRepo.getCurrentSession()?.userId)
    }

    @Test
    fun signInWithEmail_failsFastOnInvalidEmail() = runBlocking {
        val result = signInUseCase("invalid-email", "password123")
        assertFalse(result.isSuccess)
        assertNull(fakeRepo.getCurrentSession())
    }

    @Test
    fun signInWithEmail_failsFastOnShortPassword() = runBlocking {
        val result = signInUseCase("user@test.com", "123")
        assertFalse(result.isSuccess)
        assertNull(fakeRepo.getCurrentSession())
    }

    @Test
    fun signUpWithEmail_succeedsOnValidData() = runBlocking {
        val result = signUpUseCase(
            name = "Budi Santoso",
            email = "budi@test.com",
            password = "securePassword",
            confirmPassword = "securePassword"
        )
        assertTrue(result.isSuccess)
        val data = (result as AppResult.Success).data
        assertEquals("new-user-123", data.userId)
        assertEquals("Budi Santoso", data.displayName)
    }

    @Test
    fun signUpWithEmail_failsWhenPasswordsDoNotMatch() = runBlocking {
        val result = signUpUseCase(
            name = "Budi",
            email = "budi@test.com",
            password = "passwordA",
            confirmPassword = "passwordB"
        )
        assertFalse(result.isSuccess)
    }

    @Test
    fun resetPassword_succeedsOnValidEmail() = runBlocking {
        val result = resetPassUseCase("user@test.com")
        assertTrue(result.isSuccess)
    }

    @Test
    fun signOut_clearsSession() = runBlocking {
        fakeRepo.activeSession = UserSession(userId = "123", email = "test@test.com")
        assertNotNull(fakeRepo.getCurrentSession())

        val result = signOutUseCase()
        assertTrue(result.isSuccess)
        assertNull(fakeRepo.getCurrentSession())
    }

    @Test
    fun googleSignIn_rejectsBlankToken() = runBlocking {
        val result = googleSignInUseCase("   ")
        assertFalse(result.isSuccess)
    }

    @Test
    fun googleSignIn_succeedsWithToken() = runBlocking {
        val result = googleSignInUseCase("valid-google-id-token")
        assertTrue(result.isSuccess)
        assertEquals("google-user-123", (result as AppResult.Success).data.userId)
    }
}
