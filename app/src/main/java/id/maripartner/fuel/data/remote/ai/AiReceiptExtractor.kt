package id.maripartner.fuel.data.remote.ai

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.functions.Functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

interface AiReceiptExtractor {
    suspend fun extractReceipt(
        storagePath: String? = null,
        rawOcrText: String? = null,
        idempotencyKey: String? = null
    ): AppResult<AiExtractionResponseDto>
}

class AiReceiptExtractorImpl(
    private val functions: Functions? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AiReceiptExtractor {

    private fun getFunctions(): Functions = functions ?: SupabaseClientProvider.functions

    override suspend fun extractReceipt(
        storagePath: String?,
        rawOcrText: String?,
        idempotencyKey: String?
    ): AppResult<AiExtractionResponseDto> = withContext(dispatcher) {
        if (storagePath.isNullOrBlank() && rawOcrText.isNullOrBlank()) {
            return@withContext AppResult.Error(AppException.Validation("Data struk tidak boleh kosong untuk analisis AI."))
        }

        try {
            val key = idempotencyKey?.takeIf { it.isNotBlank() } ?: "ocr_${UUID.randomUUID()}"
            val requestDto = AiExtractionRequestDto(
                storagePath = storagePath,
                rawOcrText = rawOcrText,
                idempotencyKey = key
            )

            val httpResponse = getFunctions().invoke("extract-fuel-receipt", body = requestDto)
            val responseText = httpResponse.bodyAsText()
            val response = json.decodeFromString<AiExtractionResponseDto>(responseText)

            if (!response.success && response.error != null) {
                if (response.code == "AI_QUOTA_EXCEEDED") {
                    AppResult.Error(AppException.Validation(response.error))
                } else {
                    AppResult.Error(AppException.Network(response.error))
                }
            } else {
                AppResult.Success(response)
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memanggil layanan AI cloud: ${e.message}", e))
        }
    }
}
