package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.UserProfile
import id.maripartner.fuel.domain.model.UserSession
import id.maripartner.fuel.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession as SupabaseUserSession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val auth: Auth = SupabaseClientProvider.auth,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): AppResult<UserSession> = withContext(dispatcher) {
        try {
            auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
                if (!displayName.isNullOrBlank()) {
                    data = buildJsonObject {
                        put("full_name", displayName.trim())
                    }
                }
            }

            val session = auth.currentSessionOrNull()
            val user = auth.currentUserOrNull()

            if (user != null) {
                AppResult.Success(mapToUserSession(user, session))
            } else {
                AppResult.Error(
                    AppException.Authentication("Pendaftaran berhasil. Silakan periksa email Anda untuk verifikasi.")
                )
            }
        } catch (e: Exception) {
            AppResult.Error(mapAuthException(e))
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AppResult<UserSession> = withContext(dispatcher) {
        try {
            auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }

            val session = auth.currentSessionOrNull()
            val user = auth.currentUserOrNull()

            if (user != null) {
                AppResult.Success(mapToUserSession(user, session))
            } else {
                AppResult.Error(AppException.Authentication("Email atau kata sandi tidak valid"))
            }
        } catch (e: Exception) {
            AppResult.Error(mapAuthException(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<UserSession> = withContext(dispatcher) {
        try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val session = auth.currentSessionOrNull()
            val user = auth.currentUserOrNull()

            if (user != null) {
                AppResult.Success(mapToUserSession(user, session))
            } else {
                AppResult.Error(AppException.Authentication("Gagal masuk dengan Google"))
            }
        } catch (e: Exception) {
            AppResult.Error(mapAuthException(e))
        }
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> = withContext(dispatcher) {
        try {
            auth.resetPasswordForEmail(email.trim())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(mapAuthException(e))
        }
    }

    override suspend fun signOut(): AppResult<Unit> = withContext(dispatcher) {
        try {
            auth.signOut()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(mapAuthException(e))
        }
    }

    override fun getCurrentSession(): UserSession? {
        val user = auth.currentUserOrNull() ?: return null
        val session = auth.currentSessionOrNull()
        return mapToUserSession(user, session)
    }

    override fun observeAuthState(): Flow<UserSession?> {
        return auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
                        mapToUserSession(user, status.session)
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
    }

    override suspend fun getUserProfile(): AppResult<UserProfile> = withContext(dispatcher) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login"))

        try {
            val profile = SupabaseClientProvider.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            AppResult.Success(profile)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memuat profil: ${e.message}", e))
        }
    }

    override suspend fun updateUserProfile(
        displayName: String,
        themePreference: String
    ): AppResult<UserProfile> = withContext(dispatcher) {
        val userId = auth.currentUserOrNull()?.id
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login"))

        try {
            val updated = SupabaseClientProvider.postgrest["profiles"]
                .update({
                    set("display_name", displayName.trim())
                    set("theme_preference", themePreference)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            AppResult.Success(updated)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memperbarui profil: ${e.message}", e))
        }
    }

    private fun mapToUserSession(user: UserInfo, session: SupabaseUserSession?): UserSession {
        val fullName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
        return UserSession(
            userId = user.id,
            email = user.email ?: "",
            displayName = fullName ?: user.email?.substringBefore("@"),
            isEmailVerified = user.emailConfirmedAt != null,
            accessToken = session?.accessToken,
            expiresAt = session?.expiresAt?.toEpochMilliseconds()
        )
    }

    private fun mapAuthException(e: Exception): AppException {
        val msg = e.message ?: ""
        return when {
            msg.contains("Invalid login credentials", ignoreCase = true) ->
                AppException.Authentication("Email atau kata sandi tidak cocok. Silakan periksa kembali.")
            msg.contains("User already registered", ignoreCase = true) ->
                AppException.Validation("Alamat email sudah terdaftar. Silakan gunakan menu Masuk.")
            msg.contains("Password should be at least", ignoreCase = true) ->
                AppException.Validation("Kata sandi harus terdiri dari minimal 6 karakter.")
            msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                AppException.Network("Gagal terhubung ke server. Periksa koneksi internet Anda.")
            else ->
                AppException.Authentication(msg.ifBlank { "Terjadi kendala autentikasi. Silakan coba lagi." }, e)
        }
    }
}
