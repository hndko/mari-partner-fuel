package id.maripartner.fuel.domain.model

data class EcoDriverReport(
    val certificateId: String,
    val driverName: String,
    val vehicleName: String,
    val vehicleType: String = "Mobil", // Mobil, Motor, Truk
    val averageKmL: Double,
    val benchmarkKmL: Double,
    val ecoScore: Int, // 0 - 100
    val badgeTitle: String,
    val co2SavedKg: Double,
    val totalDistanceKm: Double,
    val totalLitersUsed: Double,
    val issueDate: String
)
