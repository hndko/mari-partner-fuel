package id.maripartner.fuel.data.remote.storage

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.ResumableClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

/**
 * Manager untuk interaksi dengan Supabase Private Storage bucket 'receipts'.
 */
class ReceiptStorageManager(
    private val storage: Storage? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private fun getStorage(): Storage = storage ?: SupabaseClientProvider.storage

    companion object {
        const val BUCKET_NAME = "receipts"
        const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5 MB
        val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        val DEFAULT_SIGNED_URL_TTL = 20.minutes
    }

    /**
     * Memvalidasi ukuran dan MIME type berkas sebelum diunggah.
     */
    fun validateReceiptFile(sizeBytes: Long, mimeType: String): AppResult<Unit> {
        if (sizeBytes <= 0) {
            return AppResult.Error(AppException.Validation("Berkas struk kosong."))
        }
        if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            return AppResult.Error(AppException.Validation("Ukuran foto struk melebihi batas maksimal 5 MB."))
        }
        if (!ALLOWED_MIME_TYPES.contains(mimeType.lowercase())) {
            return AppResult.Error(AppException.Validation("Format berkas harus JPEG, PNG, atau WebP."))
        }
        return AppResult.Success(Unit)
    }

    /**
     * Menghasilkan path berkas yang terisolasi per pengguna dan anti-tabrakan.
     * Format: receipts/{userId}/{UUID}.{ext}
     */
    fun generateStoragePath(userId: String, mimeType: String): String {
        require(userId.isNotBlank()) { "User ID tidak boleh kosong" }
        val extension = when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val fileUuid = UUID.randomUUID().toString()
        return "$userId/$fileUuid.$extension"
    }

    /**
     * Mengunggah berkas foto struk ke private bucket.
     */
    suspend fun uploadReceipt(
        userId: String,
        fileBytes: ByteArray,
        mimeType: String
    ): AppResult<String> = withContext(dispatcher) {
        val validation = validateReceiptFile(fileBytes.size.toLong(), mimeType)
        if (!validation.isSuccess) {
            return@withContext AppResult.Error((validation as AppResult.Error).exception)
        }

        val relativePath = generateStoragePath(userId, mimeType)

        try {
            val bucket = getStorage().from(BUCKET_NAME)
            bucket.upload(
                path = relativePath,
                data = fileBytes
            ) {
                upsert = false
            }
            AppResult.Success(relativePath)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal mengunggah foto struk: ${e.message}", e))
        }
    }

    /**
     * Menghasilkan URL bertanda tangan (Signed URL) berumur pendek untuk melihat struk privat.
     */
    suspend fun createSignedUrl(
        storagePath: String,
        expiresIn: kotlin.time.Duration = DEFAULT_SIGNED_URL_TTL
    ): AppResult<String> = withContext(dispatcher) {
        try {
            val bucket = getStorage().from(BUCKET_NAME)
            val signedUrl = bucket.createSignedUrl(
                path = storagePath,
                expiresIn = expiresIn
            )
            AppResult.Success(signedUrl)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal membuat signed URL struk: ${e.message}", e))
        }
    }

    /**
     * Menghapus berkas struk dari private bucket.
     */
    suspend fun deleteReceipt(storagePath: String): AppResult<Unit> = withContext(dispatcher) {
        try {
            val bucket = getStorage().from(BUCKET_NAME)
            bucket.delete(listOf(storagePath))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal menghapus foto struk: ${e.message}", e))
        }
    }
}
