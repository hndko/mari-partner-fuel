package id.maripartner.fuel.domain.model

/**
 * Pilihan preset harga bahan bakar standar di Indonesia (Pertamina, Shell, BP, dll).
 */
data class FuelPricePreset(
    val name: String,
    val priceIdr: Long,
    val category: String = "Bensin"
)

object IndonesianFuelPrices {
    val presets: List<FuelPricePreset> = listOf(
        FuelPricePreset("Pertalite (RON 90)", 10_000L, "Bensin Subsidi"),
        FuelPricePreset("Pertamax (RON 92)", 12_100L, "Bensin Nonsubsidi"),
        FuelPricePreset("Pertamax Turbo (RON 98)", 13_250L, "Bensin Nonsubsidi"),
        FuelPricePreset("Bio Solar (CN 48)", 6_800L, "Diesel Subsidi"),
        FuelPricePreset("Dexlite (CN 51)", 13_050L, "Diesel Nonsubsidi"),
        FuelPricePreset("Pertamina Dex (CN 53)", 13_400L, "Diesel Nonsubsidi"),
        FuelPricePreset("Shell Super (RON 92)", 12_290L, "Bensin Swasta"),
        FuelPricePreset("BP 92 (RON 92)", 12_290L, "Bensin Swasta")
    )
}
