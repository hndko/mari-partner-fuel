package id.maripartner.fuel.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.data.repository.AuthRepositoryImpl
import id.maripartner.fuel.domain.model.UserSession

/**
 * Rute-rute publik yang dapat diakses tanpa autentikasi / login.
 */
val PublicNavRoutes = setOf(
    NavRoute.Splash.route,
    NavRoute.Login.route,
    NavRoute.Register.route,
    NavRoute.ForgotPassword.route
)

/**
 * Memeriksa apakah sebuah rute bersifat publik (bebas login).
 */
fun isPublicRoute(route: String?): Boolean {
    if (route == null) return false
    return route in PublicNavRoutes
}

/**
 * Menyelesaikan destinasi awal aplikasi secara otoritatif:
 * Jika rute yang diminta memerlukan login dan sesi pengguna belum tersedia,
 * otomatis dialihkan (redirect) langsung ke halaman Login.
 */
fun resolveStartDestination(requestedRoute: String, session: UserSession?): String {
    return if (session == null && !isPublicRoute(requestedRoute)) {
        NavRoute.Login.route
    } else {
        requestedRoute
    }
}

/**
 * Middleware / Guard Komposable:
 * Memastikan layar berproteksi hanya dapat dirender jika pengguna sudah login.
 * Jika belum login, proses rendering dibatalkan seketika dan pengguna di-redirect ke LoginScreen.
 */
@Composable
fun AuthenticatedRoute(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val session = remember { AuthRepositoryImpl().getCurrentSession() }

    if (session == null) {
        LaunchedEffect(Unit) {
            navController.navigate(NavRoute.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MariPrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    } else {
        content()
    }
}
