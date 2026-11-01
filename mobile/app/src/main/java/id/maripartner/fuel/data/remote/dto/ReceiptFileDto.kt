package id.maripartner.fuel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Data Transfer Object untuk tabel 'receipt_files' di Supabase.
 */
@Serializable
data class ReceiptFileDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("storage_path") val storagePath: String,
    @SerialName("mime_type") val mimeType: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    val sha256: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
