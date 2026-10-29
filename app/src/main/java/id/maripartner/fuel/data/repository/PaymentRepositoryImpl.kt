package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.CheckoutRequestDto
import id.maripartner.fuel.data.remote.dto.CheckoutResponseDto
import id.maripartner.fuel.data.remote.dto.PaymentStatusRequestDto
import id.maripartner.fuel.data.remote.dto.PaymentStatusResponseDto
import id.maripartner.fuel.data.remote.dto.VerifyPlayPurchaseRequestDto
import id.maripartner.fuel.data.remote.dto.VerifyPlayPurchaseResponseDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.PaymentOrder
import id.maripartner.fuel.domain.model.PaymentStatus
import id.maripartner.fuel.domain.repository.PaymentRepository
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.Instant

class PaymentRepositoryImpl(
    private val functions: Functions? = null,
    private val postgrest: Postgrest? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PaymentRepository {

    private fun getFunctions(): Functions = functions ?: SupabaseClientProvider.functions
    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest

    override suspend fun createCheckoutOrder(
        planCode: String,
        provider: String
    ): AppResult<PaymentOrder> = withContext(dispatcher) {
        try {
            val requestDto = CheckoutRequestDto(planCode = planCode, provider = provider)
            val httpResponse = getFunctions().invoke("create-checkout", body = requestDto)
            val text = httpResponse.bodyAsText()
            val response = json.decodeFromString<CheckoutResponseDto>(text)

            if (!response.success) {
                return@withContext AppResult.Error(AppException.Network(response.error ?: "Gagal membuat order pembayaran"))
            }

            val expiresInstant = response.expiresAt?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }

            val order = PaymentOrder(
                paymentReference = response.paymentReference,
                provider = response.provider,
                amountIdr = response.amountIdr,
                currency = response.currency,
                planCode = response.planCode,
                planName = response.planName,
                billingPeriod = response.billingPeriod,
                productId = response.productId,
                basePlanId = response.basePlanId,
                checkoutUrl = response.checkoutUrl,
                qrisImageUrl = response.qrisImageUrl,
                expiresAt = expiresInstant
            )
            AppResult.Success(order)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal menghubungi server pembayaran: ${e.message}", e))
        }
    }

    override suspend fun verifyPlayBillingPurchase(
        purchaseToken: String,
        orderId: String,
        productId: String,
        basePlanId: String,
        paymentReference: String?
    ): AppResult<Boolean> = withContext(dispatcher) {
        try {
            val requestDto = VerifyPlayPurchaseRequestDto(
                purchaseToken = purchaseToken,
                orderId = orderId,
                productId = productId,
                basePlanId = basePlanId,
                paymentReference = paymentReference
            )
            val httpResponse = getFunctions().invoke("verify-play-purchase", body = requestDto)
            val text = httpResponse.bodyAsText()
            val response = json.decodeFromString<VerifyPlayPurchaseResponseDto>(text)

            if (!response.success) {
                return@withContext AppResult.Error(AppException.Validation(response.error ?: "Verifikasi pembelian Google Play gagal"))
            }

            AppResult.Success(response.isPro)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memverifikasi pembelian Google Play: ${e.message}", e))
        }
    }

    override suspend fun getPaymentStatus(paymentReference: String): AppResult<PaymentStatus> = withContext(dispatcher) {
        try {
            val requestDto = PaymentStatusRequestDto(paymentReference = paymentReference)
            val httpResponse = getFunctions().invoke("get-payment-status", body = requestDto)
            val text = httpResponse.bodyAsText()
            val response = json.decodeFromString<PaymentStatusResponseDto>(text)

            if (!response.success) {
                return@withContext AppResult.Error(AppException.Network(response.error ?: "Gagal memeriksa status pembayaran"))
            }

            val paidInstant = response.paidAt?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }
            val expiresInstant = response.expiresAt?.let {
                runCatching { Instant.parse(it) }.getOrNull()
            }

            val status = PaymentStatus(
                paymentReference = response.paymentReference,
                provider = response.provider,
                amountIdr = response.amountIdr,
                status = response.status,
                isPaid = response.isPaid,
                isPro = response.isPro,
                paidAt = paidInstant,
                expiresAt = expiresInstant
            )
            AppResult.Success(status)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memeriksa status: ${e.message}", e))
        }
    }

    override suspend fun getPaymentHistory(): AppResult<List<PaymentStatus>> = withContext(dispatcher) {
        try {
            val rows = getPostgrest().from("payment_transactions")
                .select()
                .decodeList<PaymentStatusResponseDto>()

            val history = rows.map { r ->
                PaymentStatus(
                    paymentReference = r.paymentReference,
                    provider = r.provider,
                    amountIdr = r.amountIdr,
                    status = r.status,
                    isPaid = r.isPaid,
                    isPro = r.isPro,
                    paidAt = r.paidAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
                    expiresAt = r.expiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
                )
            }
            AppResult.Success(history)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memuat riwayat transaksi: ${e.message}", e))
        }
    }
}
