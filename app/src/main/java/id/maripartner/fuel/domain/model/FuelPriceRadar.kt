package id.maripartner.fuel.domain.model

data class FuelBrandPrice(
    val brand: String, // Pertamina, Shell, BP, Vivo
    val fuelName: String,
    val gradeCategory: String, // RON 90, RON 92, RON 95, RON 98, Diesel
    val pricePerLiterIdr: Long,
    val isSubsidized: Boolean = false
)

data class FuelSavingsArbitrageResult(
    val gradeCategory: String,
    val tankCapacityLiters: Double,
    val brandA: FuelBrandPrice,
    val brandB: FuelBrandPrice,
    val priceDiffPerLiter: Long,
    val savingsPerFullTankIdr: Long,
    val estimatedMonthlySavingsIdr: Long, // Asumsi 4x isi sebulan
    val cheaperBrandName: String
)

object IndonesianFuelMarketData {
    val currentPrices: List<FuelBrandPrice> = listOf(
        // Pertamina
        FuelBrandPrice("Pertamina", "Pertalite", "RON 90", 10_000L, isSubsidized = true),
        FuelBrandPrice("Pertamina", "Pertamax", "RON 92", 12_100L),
        FuelBrandPrice("Pertamina", "Pertamax Green 95", "RON 95", 13_150L),
        FuelBrandPrice("Pertamina", "Pertamax Turbo", "RON 98", 13_250L),
        FuelBrandPrice("Pertamina", "Bio Solar", "Diesel", 6_800L, isSubsidized = true),
        FuelBrandPrice("Pertamina", "Dexlite (CN 51)", "Diesel", 13_050L),
        FuelBrandPrice("Pertamina", "Pertamina Dex (CN 53)", "Diesel", 13_400L),

        // Shell
        FuelBrandPrice("Shell", "Shell Super", "RON 92", 12_290L),
        FuelBrandPrice("Shell", "Shell V-Power", "RON 95", 13_300L),
        FuelBrandPrice("Shell", "Shell V-Power Nitro+", "RON 98", 13_540L),
        FuelBrandPrice("Shell", "Shell V-Power Diesel", "Diesel", 13_610L),

        // BP AKR
        FuelBrandPrice("BP", "BP 92", "RON 92", 12_290L),
        FuelBrandPrice("BP", "BP Ultimate", "RON 95", 13_300L),
        FuelBrandPrice("BP", "BP Ultimate Diesel", "Diesel", 13_610L),

        // Vivo
        FuelBrandPrice("Vivo", "Revvo 90", "RON 90", 11_990L),
        FuelBrandPrice("Vivo", "Revvo 92", "RON 92", 12_200L),
        FuelBrandPrice("Vivo", "Revvo 95", "RON 95", 13_200L)
    )
}
