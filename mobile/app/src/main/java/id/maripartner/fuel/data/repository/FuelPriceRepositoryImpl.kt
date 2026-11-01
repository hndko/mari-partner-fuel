package id.maripartner.fuel.data.repository

import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.remote.dto.FuelPriceDto
import id.maripartner.fuel.data.remote.supabase.SupabaseClientProvider
import id.maripartner.fuel.domain.model.FuelBrandPrice
import id.maripartner.fuel.domain.model.IndonesianFuelMarketData
import id.maripartner.fuel.domain.repository.FuelPriceRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FuelPriceRepositoryImpl(
    private val postgrest: Postgrest? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FuelPriceRepository {

    private fun getPostgrest(): Postgrest = postgrest ?: SupabaseClientProvider.postgrest

    override suspend fun getFuelPrices(): AppResult<List<FuelBrandPrice>> = withContext(dispatcher) {
        try {
            val response = getPostgrest().from("fuel_prices").select {
                order("brand", Order.ASCENDING)
                order("price_per_liter_idr", Order.ASCENDING)
            }.decodeList<FuelPriceDto>()

            if (response.isNotEmpty()) {
                AppResult.Success(response.map { it.toDomain() })
            } else {
                // Fallback ke preset lokal jika tabel masih kosong
                AppResult.Success(IndonesianFuelMarketData.currentPrices)
            }
        } catch (_: Exception) {
            // Graceful fallback ke preset lokal jika offline / koneksi gagal
            AppResult.Success(IndonesianFuelMarketData.currentPrices)
        }
    }

    override suspend fun updateFuelPrice(id: String, newPriceIdr: Long): AppResult<Unit> = withContext(dispatcher) {
        try {
            getPostgrest().rpc(
                "admin_update_fuel_price",
                buildJsonObject {
                    put("p_fuel_id", id)
                    put("p_new_price", newPriceIdr)
                }
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppException.Network("Gagal memperbarui tarif BBM: ${e.message}", e))
        }
    }
}
