package id.maripartner.fuel.core.common.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppConfigTest {

    @Test
    fun redactedKey_protectsSensitiveInformation() {
        assertEquals("eyJh...ey12", AppConfig.redactedKey("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.key12"))
        assertEquals("***", AppConfig.redactedKey("short"))
    }

    @Test
    fun appConfig_providesNonNullValues() {
        assertNotNull(AppConfig.supabaseUrl)
        assertNotNull(AppConfig.supabaseAnonKey)
        assertNotNull(AppConfig.googleWebClientId)
    }
}
