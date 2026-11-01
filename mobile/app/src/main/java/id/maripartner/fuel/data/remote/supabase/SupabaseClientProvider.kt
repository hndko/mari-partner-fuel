package id.maripartner.fuel.data.remote.supabase

import id.maripartner.fuel.core.common.config.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Provider singleton untuk SupabaseClient yang aman dan terkonfigurasi.
 */
object SupabaseClientProvider {

    @Volatile
    private var instance: SupabaseClient? = null

    fun getClient(): SupabaseClient {
        return instance ?: synchronized(this) {
            instance ?: createClient().also { instance = it }
        }
    }

    private fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = AppConfig.supabaseUrl,
            supabaseKey = AppConfig.supabaseAnonKey
        ) {
            httpEngine = OkHttp.create()

            install(Auth) {
                // Session dipertahankan secara otomatis oleh Supabase SDK
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }

            install(Postgrest)
            install(Storage)
            install(Functions)
        }
    }

    val auth: Auth
        get() = getClient().auth

    val postgrest: Postgrest
        get() = getClient().postgrest

    val storage: Storage
        get() = getClient().storage

    val functions: Functions
        get() = getClient().functions
}
