package id.maripartner.fuel.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavRouteTest {

    @Test
    fun navRoute_hasExpectedRoutes() {
        assertEquals("splash", NavRoute.Splash.route)
        assertEquals("login", NavRoute.Login.route)
        assertEquals("dashboard", NavRoute.Dashboard.route)
        assertEquals("transactions", NavRoute.Transactions.route)
        assertEquals("analytics", NavRoute.Analytics.route)
        assertEquals("vehicles", NavRoute.Vehicles.route)
        assertEquals("profile", NavRoute.Profile.route)
        assertEquals("reminders", NavRoute.Reminders.route)
        assertEquals("saas_admin_dashboard", NavRoute.SaasAdminDashboard.route)
    }

    @Test
    fun navRoute_formatsDynamicArguments() {
        assertEquals("transaction_detail/tx-123", NavRoute.TransactionDetail.createRoute("tx-123"))
        assertEquals("checkout/pro_monthly", NavRoute.Checkout.createRoute("pro_monthly"))
        assertEquals("vehicle_form", NavRoute.VehicleForm.createRoute())
        assertEquals("vehicle_form?id=veh-999", NavRoute.VehicleForm.createRoute("veh-999"))
        assertEquals("transaction_form", NavRoute.TransactionForm.createRoute())
        assertEquals("transaction_form?id=tx-888", NavRoute.TransactionForm.createRoute("tx-888"))
        assertEquals("reminder_form", NavRoute.ReminderForm.createRoute())
        assertEquals("reminder_form?id=rem-777", NavRoute.ReminderForm.createRoute("rem-777"))
    }

    @Test
    fun bottomNavItem_containsFiveMainTabs() {
        assertEquals(5, BottomNavItem.items.size)
        val routes = BottomNavItem.items.map { it.route }
        assertTrue(routes.contains(NavRoute.Dashboard.route))
        assertTrue(routes.contains(NavRoute.Transactions.route))
        assertTrue(routes.contains(NavRoute.Analytics.route))
        assertTrue(routes.contains(NavRoute.Vehicles.route))
        assertTrue(routes.contains(NavRoute.Profile.route))
    }
}
