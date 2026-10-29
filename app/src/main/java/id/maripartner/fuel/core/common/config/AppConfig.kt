package id.maripartner.fuel.core.common.config

import id.maripartner.fuel.BuildConfig

/**
 * Konfigurasi aplikasi yang aman dari exposure secret.
 * Mengambil nilai dari buildConfigField tanpa mengekspos kunci di repository.
 */
object AppConfig {
    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL

    val supabaseAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("placeholder") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("placeholder")

    /**
     * Redaksi key agar aman saat di-print di debug log.
     */
    fun redactedKey(key: String): String {
        if (key.length <= 8) return "***"
        return "${key.take(4)}...${key.takeLast(4)}"
    }
}
