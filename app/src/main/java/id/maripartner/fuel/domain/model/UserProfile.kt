package id.maripartner.fuel.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model domain & DTO profil pengguna dari tabel `public.profiles`.
 */
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("timezone")
    val timezone: String = "Asia/Jakarta",
    @SerialName("theme_preference")
    val themePreference: String = "system",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
