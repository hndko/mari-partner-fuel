package id.maripartner.fuel.core.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricHardwareStatus {
    AVAILABLE,
    NOT_ENROLLED,
    NO_HARDWARE,
    UNSUPPORTED
}

object BiometricAuthManager {

    private const val PREF_NAME = "mari_partner_security_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_TIMEOUT_MINUTES = "lock_timeout_minutes"
    private const val KEY_LAST_PAUSED = "last_paused_timestamp"
    private const val KEY_IS_LOCKED = "is_app_locked"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun canAuthenticate(context: Context): BiometricHardwareStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricHardwareStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricHardwareStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricHardwareStatus.NO_HARDWARE
            else -> BiometricHardwareStatus.UNSUPPORTED
        }
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        if (!enabled) {
            setAppLocked(context, false)
        }
    }

    fun getTimeoutMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_TIMEOUT_MINUTES, 0) // 0 = Langsung
    }

    fun setTimeoutMinutes(context: Context, minutes: Int) {
        getPrefs(context).edit().putInt(KEY_TIMEOUT_MINUTES, minutes).apply()
    }

    fun isAppLocked(context: Context): Boolean {
        if (!isBiometricEnabled(context)) return false
        return getPrefs(context).getBoolean(KEY_IS_LOCKED, false)
    }

    fun setAppLocked(context: Context, locked: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOCKED, locked).apply()
    }

    fun onAppPaused(context: Context) {
        if (!isBiometricEnabled(context)) return
        val now = System.currentTimeMillis()
        getPrefs(context).edit().putLong(KEY_LAST_PAUSED, now).apply()
    }

    fun onAppResumed(context: Context) {
        if (!isBiometricEnabled(context)) {
            setAppLocked(context, false)
            return
        }

        val lastPaused = getPrefs(context).getLong(KEY_LAST_PAUSED, 0L)
        if (lastPaused == 0L) {
            // First run or clean state
            return
        }

        val timeoutMinutes = getTimeoutMinutes(context)
        val elapsedMillis = System.currentTimeMillis() - lastPaused
        val timeoutMillis = timeoutMinutes * 60 * 1000L

        if (elapsedMillis >= timeoutMillis) {
            setAppLocked(context, true)
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Buka Kunci Mari Partner Fuel",
        subtitle: String = "Sentuh sensor sidik jari atau gunakan PIN/Pola untuk melanjutkan",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val status = canAuthenticate(activity)
        if (status != BiometricHardwareStatus.AVAILABLE) {
            setAppLocked(activity, false)
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    setAppLocked(activity, false)
                    getPrefs(activity).edit().putLong(KEY_LAST_PAUSED, System.currentTimeMillis()).apply()
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                        errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ||
                        errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT
                    ) {
                        setAppLocked(activity, false)
                        onSuccess()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt remains open for next attempt
                }
            }
        )

        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }
}
