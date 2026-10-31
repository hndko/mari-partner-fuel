package id.maripartner.fuel.core.location

data class SpbuLocation(
    val id: String,
    val name: String,
    val brand: String, // Pertamina, Shell, BP, Vivo
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String
)

data class NearestSpbuResult(
    val spbu: SpbuLocation,
    val distanceMeters: Float,
    val isWithinGeofence: Boolean // <= 250m
)

object SpbuDirectory {
    val indonesiaSpbuList: List<SpbuLocation> = listOf(
        // DKI Jakarta & Sekitarnya
        SpbuLocation("spbu_1", "SPBU Pertamina 31.129.02 Kuningan", "Pertamina", -6.2297, 106.8295, "Jl. HR Rasuna Said, Kuningan", "Jakarta Selatan"),
        SpbuLocation("spbu_2", "SPBU Pertamina 31.102.02 Abdul Muis", "Pertamina", -6.1818, 106.8190, "Jl. Abdul Muis No. 68", "Jakarta Pusat"),
        SpbuLocation("spbu_3", "SPBU Pertamina MT Haryono", "Pertamina", -6.2428, 106.8617, "Jl. MT Haryono Kav 18", "Jakarta Timur"),
        SpbuLocation("spbu_4", "SPBU Shell Gatot Subroto", "Shell", -6.2365, 106.8213, "Jl. Gatot Subroto Kav 99", "Jakarta Selatan"),
        SpbuLocation("spbu_5", "SPBU Shell Antasari", "Shell", -6.2750, 106.8080, "Jl. Pangeran Antasari No. 32", "Jakarta Selatan"),
        SpbuLocation("spbu_6", "SPBU BP AKR TB Simatupang", "BP", -6.2941, 106.8228, "Jl. TB Simatupang No. 12", "Jakarta Selatan"),
        SpbuLocation("spbu_7", "SPBU BP AKR Meruya", "BP", -6.1923, 106.7456, "Jl. Meruya Ilir Raya", "Jakarta Barat"),
        SpbuLocation("spbu_8", "SPBU Vivo MT Haryono", "Vivo", -6.2435, 106.8580, "Jl. MT Haryono No. 5", "Jakarta Selatan"),
        SpbuLocation("spbu_9", "SPBU Vivo Bintaro Sektor 7", "Vivo", -6.2820, 106.7180, "Bintaro Jaya Sektor 7", "Tangerang Selatan"),
        
        // Bandung
        SpbuLocation("spbu_10", "SPBU Pertamina Dago 34.401.30", "Pertamina", -6.8850, 107.6140, "Jl. Ir. H. Juanda No. 145", "Bandung"),
        SpbuLocation("spbu_11", "SPBU Shell Pasteur", "Shell", -6.8970, 107.5850, "Jl. Dr. Djunjunan No. 120", "Bandung"),
        SpbuLocation("spbu_12", "SPBU BP Soekarno Hatta", "BP", -6.9450, 107.6320, "Jl. Soekarno Hatta No. 450", "Bandung"),

        // Surabaya
        SpbuLocation("spbu_13", "SPBU Pertamina Mayjen Sungkono", "Pertamina", -7.2880, 112.7150, "Jl. Mayjen Sungkono No. 89", "Surabaya"),
        SpbuLocation("spbu_14", "SPBU Shell Kertajaya", "Shell", -7.2790, 112.7630, "Jl. Raya Kertajaya Indah", "Surabaya"),
        SpbuLocation("spbu_15", "SPBU BP Gubeng", "BP", -7.2690, 112.7520, "Jl. Raya Gubeng No. 54", "Surabaya"),

        // Semarang & Yogyakarta
        SpbuLocation("spbu_16", "SPBU Pertamina Pemuda", "Pertamina", -6.9780, 110.4180, "Jl. Pemuda No. 102", "Semarang"),
        SpbuLocation("spbu_17", "SPBU Pertamina Kaliurang", "Pertamina", -7.7650, 110.3800, "Jl. Kaliurang Km 5.2", "Yogyakarta"),
        SpbuLocation("spbu_18", "SPBU Shell Gejayan", "Shell", -7.7600, 110.3910, "Jl. Affandi / Gejayan No. 28", "Yogyakarta"),

        // Medan & Makassar & Bali
        SpbuLocation("spbu_19", "SPBU Pertamina Gatot Subroto Medan", "Pertamina", 3.5850, 98.6570, "Jl. Gatot Subroto No. 180", "Medan"),
        SpbuLocation("spbu_20", "SPBU Pertamina Urip Sumoharjo", "Pertamina", -5.1380, 119.4420, "Jl. Urip Sumoharjo", "Makassar"),
        SpbuLocation("spbu_21", "SPBU Pertamina Sunset Road", "Pertamina", -8.7050, 115.1820, "Jl. Sunset Road No. 88", "Kuta, Bali")
    )
}
