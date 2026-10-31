package id.maripartner.fuel.core.navigation

import id.maripartner.fuel.domain.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthGuardTest {

    @Test
    fun isPublicRoute_returnsTrueForPublicRoutes() {
        assertTrue(isPublicRoute(NavRoute.Splash.route))
        assertTrue(isPublicRoute(NavRoute.Login.route))
        assertTrue(isPublicRoute(NavRoute.Register.route))
        assertTrue(isPublicRoute(NavRoute.ForgotPassword.route))
    }

    @Test
    fun isPublicRoute_returnsFalseForProtectedRoutesAndNull() {
        assertFalse(isPublicRoute(null))
        assertFalse(isPublicRoute(NavRoute.Dashboard.route))
        assertFalse(isPublicRoute(NavRoute.Transactions.route))
        assertFalse(isPublicRoute(NavRoute.Analytics.route))
        assertFalse(isPublicRoute(NavRoute.Vehicles.route))
        assertFalse(isPublicRoute(NavRoute.Profile.route))
        assertFalse(isPublicRoute(NavRoute.ProPlans.route))
        assertFalse(isPublicRoute(NavRoute.TripCalculator.route))
        assertFalse(isPublicRoute(NavRoute.ReceiptScan.route))
        assertFalse(isPublicRoute(NavRoute.SaasAdminDashboard.route))
    }

    @Test
    fun resolveStartDestination_redirectsToLoginWhenUnauthenticatedOnProtectedRoute() {
        val resolved = resolveStartDestination(
            requestedRoute = NavRoute.Dashboard.route,
            session = null
        )
        assertEquals(NavRoute.Login.route, resolved)

        val resolvedAnalytics = resolveStartDestination(
            requestedRoute = NavRoute.Analytics.route,
            session = null
        )
        assertEquals(NavRoute.Login.route, resolvedAnalytics)

        val resolvedProfile = resolveStartDestination(
            requestedRoute = NavRoute.Profile.route,
            session = null
        )
        assertEquals(NavRoute.Login.route, resolvedProfile)
    }

    @Test
    fun resolveStartDestination_keepsRequestedRouteWhenUnauthenticatedOnPublicRoute() {
        val resolvedSplash = resolveStartDestination(
            requestedRoute = NavRoute.Splash.route,
            session = null
        )
        assertEquals(NavRoute.Splash.route, resolvedSplash)

        val resolvedLogin = resolveStartDestination(
            requestedRoute = NavRoute.Login.route,
            session = null
        )
        assertEquals(NavRoute.Login.route, resolvedLogin)

        val resolvedRegister = resolveStartDestination(
            requestedRoute = NavRoute.Register.route,
            session = null
        )
        assertEquals(NavRoute.Register.route, resolvedRegister)
    }

    @Test
    fun resolveStartDestination_allowsProtectedRouteWhenAuthenticated() {
        val mockSession = UserSession(
            userId = "user-123",
            email = "user@example.com"
        )

        val resolved = resolveStartDestination(
            requestedRoute = NavRoute.Dashboard.route,
            session = mockSession
        )
        assertEquals(NavRoute.Dashboard.route, resolved)

        val resolvedVehicles = resolveStartDestination(
            requestedRoute = NavRoute.Vehicles.route,
            session = mockSession
        )
        assertEquals(NavRoute.Vehicles.route, resolvedVehicles)
    }
}
