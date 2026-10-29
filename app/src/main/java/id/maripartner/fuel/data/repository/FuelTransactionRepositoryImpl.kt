package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.FuelTransactionDto
import id.maripartner.fuel.data.remote.dto.ReceiptFileDto
import id.maripartner.fuel.data.remote.storage.ReceiptStorageManager
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.SortOrder
import id.maripartner.fuel.domain.model.TransactionFilter
import id.maripartner.fuel.domain.repository.FuelTransactionRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneOffset

/**
 * Implementasi repositori transaksi pengisian BBM menggunakan Supabase Postgrest, Auth, dan Storage.
 */
class FuelTransactionRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val auth: Auth? = null,
    private val storageManager: ReceiptStorageManager = ReceiptStorageManager(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FuelTransactionRepository {

    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest
    private fun getAuth(): Auth = auth ?: SupabaseClientProvider.auth

    private fun getCurrentUserId(): String? {
        return getAuth().currentUserOrNull()?.id
    }

    override suspend fun getTransactions(
        filter: TransactionFilter,
        limit: Int,
        offset: Int
    ): AppResult<List<FuelTransaction>> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val response = getPostgrest().from("fuel_transactions").select(
                Columns.raw("*, vehicles(*), receipt_files(*)")
            ) {
                filter {
                    eq("user_id", userId)
                    filter.vehicleId?.takeIf { it.isNotBlank() }?.let { eq("vehicle_id", it) }
                    filter.fuelType?.takeIf { it.isNotBlank() }?.let { eq("fuel_type", it) }
                    filter.startDate?.let {
                        val startIso = it.atStartOfDay(ZoneOffset.UTC).toInstant().toString()
                        gte("transaction_at", startIso)
                    }
                    filter.endDate?.let {
                        val endIso = it.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString()
                        lte("transaction_at", endIso)
                    }
                    if (filter.query.isNotBlank()) {
                        ilike("station_name", "%${filter.query.trim()}%")
                    }
                }
                val sortDirection = if (filter.sortOrder == SortOrder.NEWEST_FIRST) {
                    Order.DESCENDING
                } else {
                    Order.ASCENDING
                }
                order("transaction_at", sortDirection)
                range(from = offset.toLong(), to = (offset + limit - 1).toLong())
            }.decodeList<FuelTransactionDto>()

            val domainList = response.map { dto ->
                dto.toDomain()
            }
            AppResult.Success(domainList)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memuat riwayat transaksi: ${e.message}", e))
        }
    }

    override suspend fun getTransactionById(id: String): AppResult<FuelTransaction> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            val dto = getPostgrest().from("fuel_transactions").select(
                Columns.raw("*, vehicles(*), receipt_files(*)")
            ) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<FuelTransactionDto>()

            if (dto != null) {
                var signedUrl: String? = null
                val storagePath = dto.receiptFiles?.storagePath
                if (!storagePath.isNullOrBlank()) {
                    val urlResult = storageManager.createSignedUrl(storagePath)
                    if (urlResult is AppResult.Success) {
                        signedUrl = urlResult.data
                    }
                }
                AppResult.Success(dto.toDomain(signedReceiptUrl = signedUrl))
            } else {
                AppResult.Error(AppException.NotFound("Transaksi tidak ditemukan."))
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal mengambil detail transaksi: ${e.message}", e))
        }
    }

    override suspend fun createTransaction(
        transaction: FuelTransaction,
        receiptBytes: ByteArray?,
        receiptMimeType: String?
    ): AppResult<FuelTransaction> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        var uploadedStoragePath: String? = null
        var createdReceiptFileId: String? = null

        try {
            // 1. Unggah foto struk jika dilampirkan
            if (receiptBytes != null && receiptMimeType != null) {
                val uploadResult = storageManager.uploadReceipt(userId, receiptBytes, receiptMimeType)
                if (uploadResult is AppResult.Error) {
                    return@withContext AppResult.Error(uploadResult.exception)
                }
                uploadedStoragePath = (uploadResult as AppResult.Success).data

                // Simpan metadata struk ke receipt_files
                val receiptDto = ReceiptFileDto(
                    userId = userId,
                    storagePath = uploadedStoragePath,
                    mimeType = receiptMimeType,
                    sizeBytes = receiptBytes.size.toLong()
                )
                val insertedReceipt = getPostgrest().from("receipt_files").insert(receiptDto) {
                    select()
                }.decodeSingle<ReceiptFileDto>()
                createdReceiptFileId = insertedReceipt.id
            }

            // 2. Simpan transaksi
            val txWithReceipt = transaction.copy(receiptFileId = createdReceiptFileId)
            val dto = FuelTransactionDto.fromDomain(txWithReceipt, userId = userId)

            val created = getPostgrest().from("fuel_transactions").insert(dto) {
                select(Columns.raw("*, vehicles(*), receipt_files(*)"))
            }.decodeSingle<FuelTransactionDto>()

            AppResult.Success(created.toDomain())
        } catch (e: Exception) {
            // Cleanup berkas yatim jika penyimpanan transaksi gagal
            if (uploadedStoragePath != null) {
                storageManager.deleteReceipt(uploadedStoragePath)
                if (createdReceiptFileId != null) {
                    try {
                        getPostgrest().from("receipt_files").delete {
                            filter {
                                eq("id", createdReceiptFileId)
                                eq("user_id", userId)
                            }
                        }
                    } catch (_: Exception) { }
                }
            }
            AppResult.Error(AppException.Network("Gagal menyimpan transaksi BBM: ${e.message}", e))
        }
    }

    override suspend fun updateTransaction(
        transaction: FuelTransaction,
        newReceiptBytes: ByteArray?,
        newReceiptMimeType: String?,
        removeReceipt: Boolean
    ): AppResult<FuelTransaction> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            var activeReceiptFileId = transaction.receiptFileId

            // Kasus 1: Permintaan menghapus lampiran struk
            if (removeReceipt && activeReceiptFileId != null) {
                activeReceiptFileId = null
            }

            // Kasus 2: Mengunggah foto struk baru pengganti/tambahan
            if (newReceiptBytes != null && newReceiptMimeType != null) {
                val uploadResult = storageManager.uploadReceipt(userId, newReceiptBytes, newReceiptMimeType)
                if (uploadResult is AppResult.Error) {
                    return@withContext AppResult.Error(uploadResult.exception)
                }
                val newStoragePath = (uploadResult as AppResult.Success).data

                val receiptDto = ReceiptFileDto(
                    userId = userId,
                    storagePath = newStoragePath,
                    mimeType = newReceiptMimeType,
                    sizeBytes = newReceiptBytes.size.toLong()
                )
                val insertedReceipt = getPostgrest().from("receipt_files").insert(receiptDto) {
                    select()
                }.decodeSingle<ReceiptFileDto>()
                activeReceiptFileId = insertedReceipt.id
            }

            val updatedDomain = transaction.copy(receiptFileId = activeReceiptFileId)
            val dto = FuelTransactionDto.fromDomain(updatedDomain, userId = userId)

            val updated = getPostgrest().from("fuel_transactions").update(dto) {
                filter {
                    eq("id", transaction.id)
                    eq("user_id", userId)
                }
                select(Columns.raw("*, vehicles(*), receipt_files(*)"))
            }.decodeSingle<FuelTransactionDto>()

            AppResult.Success(updated.toDomain())
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memperbarui transaksi: ${e.message}", e))
        }
    }

    override suspend fun deleteTransaction(id: String): AppResult<Unit> = withContext(dispatcher) {
        val userId = getCurrentUserId()
            ?: return@withContext AppResult.Error(AppException.Authentication("Pengguna belum login."))

        try {
            // Ambil detail struk sebelum dihapus
            val existing = getPostgrest().from("fuel_transactions").select(
                Columns.raw("id, receipt_file_id, receipt_files(storage_path)")
            ) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<FuelTransactionDto>()

            getPostgrest().from("fuel_transactions").delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }

            // Bersihkan file struk jika ada
            val receiptId = existing?.receiptFileId
            val storagePath = existing?.receiptFiles?.storagePath
            if (!receiptId.isNullOrBlank()) {
                try {
                    getPostgrest().from("receipt_files").delete {
                        filter {
                            eq("id", receiptId)
                            eq("user_id", userId)
                        }
                    }
                    if (!storagePath.isNullOrBlank()) {
                        storageManager.deleteReceipt(storagePath)
                    }
                } catch (_: Exception) { }
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal menghapus transaksi: ${e.message}", e))
        }
    }

    override suspend fun getReceiptSignedUrl(storagePath: String): AppResult<String> {
        return storageManager.createSignedUrl(storagePath)
    }
}
