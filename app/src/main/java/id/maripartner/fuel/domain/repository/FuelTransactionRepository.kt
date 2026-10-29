package id.maripartner.fuel.domain.repository

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.TransactionFilter

/**
 * Kontrak repositori untuk operasi transaksi BBM dan lampiran struk privat.
 */
interface FuelTransactionRepository {

    /**
     * Mengambil daftar transaksi BBM dengan filter, pengurutan, dan pagination server-side.
     */
    suspend fun getTransactions(
        filter: TransactionFilter,
        limit: Int = 20,
        offset: Int = 0
    ): AppResult<List<FuelTransaction>>

    /**
     * Mengambil detail transaksi berdasarkan ID, beserta relasi kendaraan & berkas struk.
     */
    suspend fun getTransactionById(id: String): AppResult<FuelTransaction>

    /**
     * Membuat transaksi baru beserta upload lampiran struk opsional.
     */
    suspend fun createTransaction(
        transaction: FuelTransaction,
        receiptBytes: ByteArray? = null,
        receiptMimeType: String? = null
    ): AppResult<FuelTransaction>

    /**
     * Memperbarui transaksi dan menangani penggantian atau penghapusan berkas struk secara aman.
     */
    suspend fun updateTransaction(
        transaction: FuelTransaction,
        newReceiptBytes: ByteArray? = null,
        newReceiptMimeType: String? = null,
        removeReceipt: Boolean = false
    ): AppResult<FuelTransaction>

    /**
     * Menghapus transaksi beserta membersihkan metadata struk yatim jika aman.
     */
    suspend fun deleteTransaction(id: String): AppResult<Unit>

    /**
     * Menghasilkan signed URL berumur pendek untuk menampilkan foto struk privat.
     */
    suspend fun getReceiptSignedUrl(storagePath: String): AppResult<String>
}
