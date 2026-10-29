package id.maripartner.fuel.core.navigation

/**
 * Rute navigasi aplikasi Mari Partner Fuel.
 */
sealed class NavRoute(val route: String) {
    // Auth & Splash
    data object Splash : NavRoute("splash")
    data object Login : NavRoute("login")
    data object Register : NavRoute("register")
    data object ForgotPassword : NavRoute("forgot_password")

    // Main 5 Bottom Tabs
    data object Dashboard : NavRoute("dashboard")
    data object Transactions : NavRoute("transactions")
    data object Analytics : NavRoute("analytics")
    data object Vehicles : NavRoute("vehicles")
    data object Profile : NavRoute("profile")

    // Vehicle Flows
    data object VehicleForm : NavRoute("vehicle_form?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "vehicle_form?id=$id" else "vehicle_form"
    }

    // OCR & Transaction Flows
    data object ReceiptScan : NavRoute("receipt_scan")
    data object ScanReview : NavRoute("scan_review")
    data object TransactionForm : NavRoute("transaction_form?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "transaction_form?id=$id" else "transaction_form"
    }
    data object TransactionDetail : NavRoute("transaction_detail/{id}") {
        fun createRoute(id: String) = "transaction_detail/$id"
    }

    // Maintenance Reminders
    data object Reminders : NavRoute("reminders")
    data object ReminderForm : NavRoute("reminder_form?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "reminder_form?id=$id" else "reminder_form"
    }

    // Pro Subscription & Checkout
    data object ProPlans : NavRoute("pro_plans")
    data object Checkout : NavRoute("checkout/{planId}") {
        fun createRoute(planId: String) = "checkout/$planId"
    }
    data object QrisPayment : NavRoute("qris_payment/{paymentReference}") {
        fun createRoute(paymentReference: String) = "qris_payment/$paymentReference"
    }
    data object PaymentStatus : NavRoute("payment_status/{paymentReference}") {
        fun createRoute(paymentReference: String) = "payment_status/$paymentReference"
    }
}
