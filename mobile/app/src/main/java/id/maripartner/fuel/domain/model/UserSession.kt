package id.maripartner.fuel.domain.model

import kotlinx.serialization.Serializable

/**
 * Model domain sesi pengguna terautentikasi.
 */
@Serializable
data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
    val accessToken: String? = null,
    val expiresAt: Long? = null
)
