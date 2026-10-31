package id.maripartner.fuel

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import id.maripartner.fuel.core.designsystem.component.BiometricLockOverlay
import id.maripartner.fuel.core.designsystem.theme.MariPartnerFuelTheme
import id.maripartner.fuel.core.navigation.AppNavHost
import id.maripartner.fuel.core.security.BiometricAuthManager

class MainActivity : FragmentActivity() {

    private var isAppLockedState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startRoute = intent.getStringExtra("start_route") ?: id.maripartner.fuel.core.navigation.NavRoute.Splash.route

        setContent {
            MariPartnerFuelTheme {
                if (isAppLockedState) {
                    BiometricLockOverlay(
                        onUnlockClick = {
                            requestBiometricUnlock()
                        }
                    )
                } else {
                    AppNavHost(startDestination = startRoute)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BiometricAuthManager.onAppResumed(this)
        if (BiometricAuthManager.isAppLocked(this)) {
            isAppLockedState = true
            requestBiometricUnlock()
        } else {
            isAppLockedState = false
        }
    }

    override fun onPause() {
        super.onPause()
        BiometricAuthManager.onAppPaused(this)
    }

    private fun requestBiometricUnlock() {
        BiometricAuthManager.authenticate(
            activity = this,
            title = "Buka Kunci Mari Partner Fuel",
            subtitle = "Autentikasi sidik jari atau biometrik Anda untuk melanjutkan",
            onSuccess = {
                isAppLockedState = false
            },
            onError = {
                // Keep overlay displayed, user can tap unlock button to retry
            }
        )
    }
}
