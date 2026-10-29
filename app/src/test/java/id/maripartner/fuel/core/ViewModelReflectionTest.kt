package id.maripartner.fuel.core

import android.app.Application
import id.maripartner.fuel.feature.analytics.AnalyticsViewModel
import id.maripartner.fuel.feature.auth.AuthViewModel
import id.maripartner.fuel.feature.dashboard.DashboardViewModel
import id.maripartner.fuel.feature.receipt_scan.ReceiptScanViewModel
import id.maripartner.fuel.feature.reminders.RemindersViewModel
import id.maripartner.fuel.feature.subscription.SubscriptionViewModel
import id.maripartner.fuel.feature.subscription.checkout.CheckoutViewModel
import id.maripartner.fuel.feature.transactions.TransactionDetailViewModel
import id.maripartner.fuel.feature.transactions.TransactionFormViewModel
import id.maripartner.fuel.feature.transactions.TransactionsViewModel
import id.maripartner.fuel.feature.vehicles.VehicleFormViewModel
import id.maripartner.fuel.feature.vehicles.VehiclesViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Pengujian refleksi konstruktor ViewModel untuk mencegah runtime crash:
 * NoSuchMethodException saat diinstansiasi oleh androidx.lifecycle.ViewModelProvider
 */
class ViewModelReflectionTest {

    @Test
    fun `RemindersViewModel must have Application constructor for AndroidViewModelFactory`() {
        val constructor = RemindersViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
    }

    @Test
    fun `standard ViewModels must have parameterless constructor for NewInstanceFactory`() {
        assertNotNull(AnalyticsViewModel::class.java.getConstructor())
        assertNotNull(AuthViewModel::class.java.getConstructor())
        assertNotNull(DashboardViewModel::class.java.getConstructor())
        assertNotNull(ReceiptScanViewModel::class.java.getConstructor())
        assertNotNull(SubscriptionViewModel::class.java.getConstructor())
        assertNotNull(CheckoutViewModel::class.java.getConstructor())
        assertNotNull(TransactionDetailViewModel::class.java.getConstructor())
        assertNotNull(TransactionFormViewModel::class.java.getConstructor())
        assertNotNull(TransactionsViewModel::class.java.getConstructor())
        assertNotNull(VehicleFormViewModel::class.java.getConstructor())
        assertNotNull(VehiclesViewModel::class.java.getConstructor())
    }
}
