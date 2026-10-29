package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.UserProfile
import id.maripartner.fuel.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak abstraksi repositori autentikasi pengguna.
 */
interface AuthRepository {

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String? = null
    ): AppResult<UserSession>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): AppResult<UserSession>

    suspend fun signInWithGoogle(
        idToken: String
    ): AppResult<UserSession>

    suspend fun sendPasswordReset(
        email: String
    ): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>

    fun getCurrentSession(): UserSession?

    fun observeAuthState(): Flow<UserSession?>

    suspend fun getUserProfile(): AppResult<UserProfile>

    suspend fun updateUserProfile(
        displayName: String,
        themePreference: String
    ): AppResult<UserProfile>
}
