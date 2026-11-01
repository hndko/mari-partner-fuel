package id.maripartner.fuel.domain.model.owner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model catatan transaksi pembayaran langganan untuk audit Pemilik SaaS.
 */
@Serializable
data class OwnerPaymentTx(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("user_display_name")
    val userDisplayName: String = "Pengguna Mari",
    @SerialName("provider")
    val provider: String = "qrispy",
    @SerialName("payment_reference")
    val paymentReference: String = "",
    @SerialName("amount_idr")
    val amountIdr: Long = 0L,
    @SerialName("status")
    val status: String = "paid",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("paid_at")
    val paidAt: String? = null,
    @SerialName("plan_name")
    val planName: String = "Paket Pro"
)
