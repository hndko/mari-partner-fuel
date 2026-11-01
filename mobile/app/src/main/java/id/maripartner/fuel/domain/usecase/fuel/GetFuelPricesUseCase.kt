package id.maripartner.fuel.domain.usecase.fuel

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.data.repository.FuelPriceRepositoryImpl
import id.maripartner.fuel.domain.model.FuelBrandPrice
import id.maripartner.fuel.domain.repository.FuelPriceRepository

class GetFuelPricesUseCase(
    private val repository: FuelPriceRepository = FuelPriceRepositoryImpl()
) {
    suspend operator fun invoke(): AppResult<List<FuelBrandPrice>> {
        return repository.getFuelPrices()
    }

    /**
     * Mencocokkan nama bahan bakar dengan harga per liter aktif.
     */
    fun findPriceForFuel(fuelName: String, prices: List<FuelBrandPrice>): Long? {
        val clean = fuelName.trim().lowercase()
        if (clean.isBlank()) return null

        // 1. Exact match by fuelName
        prices.firstOrNull { it.fuelName.equals(fuelName, ignoreCase = true) }?.let {
            return it.pricePerLiterIdr
        }

        // 2. Intelligent keyword match
        return when {
            clean.contains("pertalite") || clean.contains("ron 90") -> {
                prices.firstOrNull { it.fuelName.contains("Pertalite", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("pertamax turbo") || clean.contains("ron 98") -> {
                prices.firstOrNull { it.fuelName.contains("Pertamax Turbo", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("pertamax green") -> {
                prices.firstOrNull { it.fuelName.contains("Green", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("pertamax") || clean.contains("ron 92") -> {
                prices.firstOrNull { it.fuelName.equals("Pertamax", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("bio solar") || clean.contains("biosolar") || clean.contains("solar") -> {
                prices.firstOrNull { it.fuelName.contains("Solar", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("dexlite") -> {
                prices.firstOrNull { it.fuelName.contains("Dexlite", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("pertamina dex") || clean.contains("dex") -> {
                prices.firstOrNull { it.fuelName.contains("Pertamina Dex", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("shell super") -> {
                prices.firstOrNull { it.fuelName.contains("Shell Super", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("v-power nitro") -> {
                prices.firstOrNull { it.fuelName.contains("Nitro", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("v-power diesel") -> {
                prices.firstOrNull { it.fuelName.contains("V-Power Diesel", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("v-power") -> {
                prices.firstOrNull { it.fuelName.equals("Shell V-Power", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("bp 92") -> {
                prices.firstOrNull { it.fuelName.contains("BP 92", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("bp ultimate diesel") -> {
                prices.firstOrNull { it.fuelName.contains("BP Ultimate Diesel", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("bp ultimate") -> {
                prices.firstOrNull { it.fuelName.contains("BP Ultimate", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("revvo 90") -> {
                prices.firstOrNull { it.fuelName.contains("Revvo 90", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("revvo 92") -> {
                prices.firstOrNull { it.fuelName.contains("Revvo 92", ignoreCase = true) }?.pricePerLiterIdr
            }
            clean.contains("revvo 95") -> {
                prices.firstOrNull { it.fuelName.contains("Revvo 95", ignoreCase = true) }?.pricePerLiterIdr
            }
            else -> null
        }
    }
}
