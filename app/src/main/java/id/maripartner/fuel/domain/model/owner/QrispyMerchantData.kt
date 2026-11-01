package id.maripartner.fuel.domain.model.owner

/**
 * Representasi saldo merchant QRISpy resmi dari gateway API.
 */
data class QrispyMerchantBalance(
    val merchantId: String = "",
    val merchantName: String = "Mari Partner Fuel",
    val balanceIdr: Long = 0L,
    val currency: String = "IDR"
)

/**
 * Representasi transaksi/mutasi langsung dari gateway QRISpy.
 */
data class QrispyMerchantTransaction(
    val qrisId: String = "",
    val amountIdr: Long = 0L,
    val status: String = "pending",
    val qrisImageUrl: String? = null,
    val createdAt: String = "",
    val expiredAt: String = "",
    val paymentReference: String = "",
    val generatedVia: String = "api"
)

/**
 * Agregasi lengkap saldo dan mutasi QRISpy gateway.
 */
data class QrispyMerchantData(
    val balance: QrispyMerchantBalance = QrispyMerchantBalance(),
    val transactions: List<QrispyMerchantTransaction> = emptyList()
)
