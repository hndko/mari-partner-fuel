package id.maripartner.fuel.data.migration

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MigrationSqlTest {

    private val migrationDir: File by lazy {
        val candidates = listOf(
            File("../supabase/migrations"),
            File("supabase/migrations"),
            File("../../supabase/migrations")
        )
        candidates.firstOrNull { it.exists() } ?: File("supabase/migrations")
    }

    @Test
    fun migrationDirectoryAndFiles_exist() {
        assertTrue("Direktori supabase/migrations harus ada", migrationDir.exists())
        val schemaFile = File(migrationDir, "20261029000001_initial_schema.sql")
        val rlsFile = File(migrationDir, "20261029000002_rls_and_storage.sql")
        val tahap3File = File(migrationDir, "20261029000003_vehicles_and_transactions_tahap3.sql")
        val tahap4File = File(migrationDir, "20261029000004_receipt_scan_jobs_policy.sql")
        val tahap5File = File(migrationDir, "20261029000005_maintenance_reminders_enhancement.sql")
        val tahap6File = File(migrationDir, "20261029000006_subscription_entitlements_and_quota.sql")
        val tahap7File = File(migrationDir, "20261029000007_payment_integration_and_activation.sql")
        val tahap8File = File(migrationDir, "20261029000008_security_hardening_and_integrity.sql")
        val tahap10File = File(migrationDir, "20261030000002_saas_admin_owner_dashboard.sql")
        val tahap11File = File(migrationDir, "20261030000003_saas_owner_plan_and_tx_management.sql")

        assertTrue("File initial_schema.sql harus ada", schemaFile.exists())
        assertTrue("File rls_and_storage.sql harus ada", rlsFile.exists())
        assertTrue("File vehicles_and_transactions_tahap3.sql harus ada", tahap3File.exists())
        assertTrue("File receipt_scan_jobs_policy.sql harus ada", tahap4File.exists())
        assertTrue("File maintenance_reminders_enhancement.sql harus ada", tahap5File.exists())
        assertTrue("File subscription_entitlements_and_quota.sql harus ada", tahap6File.exists())
        assertTrue("File payment_integration_and_activation.sql harus ada", tahap7File.exists())
        assertTrue("File security_hardening_and_integrity.sql harus ada", tahap8File.exists())
        assertTrue("File saas_admin_owner_dashboard.sql harus ada", tahap10File.exists())
        assertTrue("File saas_owner_plan_and_tx_management.sql harus ada", tahap11File.exists())
    }

    @Test
    fun migrationTahap5_enhancesMaintenanceReminders() {
        val file = File(migrationDir, "20261029000005_maintenance_reminders_enhancement.sql")
        val content = file.readText()

        assertTrue("Harus menambahkan composite foreign key vehicle user", content.contains("fk_maintenance_reminders_vehicle_user"))
        assertTrue("Harus menambahkan completed_at", content.contains("completed_at timestamptz"))
        assertTrue("Harus menambahkan last_notified_at", content.contains("last_notified_at timestamptz"))
        assertTrue("Harus ada check constraint pemicu jatuh tempo", content.contains("chk_maintenance_reminders_trigger"))
        assertTrue("Harus ada indeks jatuh tempo", content.contains("idx_maintenance_reminders_due"))
    }

    @Test
    fun migrationTahap4_addsReceiptScanJobsUpdatePolicy() {
        val file = File(migrationDir, "20261029000004_receipt_scan_jobs_policy.sql")
        val content = file.readText()

        assertTrue("Harus menambahkan update policy untuk receipt_scan_jobs", content.contains("receipt_scan_jobs_update_own"))
        assertTrue("Policy harus memeriksa auth.uid() = user_id", content.contains("auth.uid() = user_id"))
    }

    @Test
    fun migrationTahap3_addsColumnsAndOdometerTrigger() {
        val file = File(migrationDir, "20261029000003_vehicles_and_transactions_tahap3.sql")
        val content = file.readText()

        assertTrue("Harus menambahkan production_year", content.contains("production_year integer"))
        assertTrue("Harus menambahkan notes pada vehicles", content.contains("notes text"))
        assertTrue("Harus melonggarkan volume_liters drop not null", content.contains("alter column volume_liters drop not null"))
        assertTrue("Harus memastikan total_amount_idr > 0", content.contains("total_amount_idr > 0"))
        assertTrue("Harus ada indeks pencarian SPBU", content.contains("idx_fuel_transactions_station"))
        assertTrue("Harus ada trigger odometer kendaraan", content.contains("trg_fuel_transactions_odometer"))
    }

    @Test
    fun initialSchema_containsAllThirteenTables() {
        val schemaFile = File(migrationDir, "20261029000001_initial_schema.sql")
        val content = schemaFile.readText()

        val expectedTables = listOf(
            "public.profiles",
            "public.vehicles",
            "public.receipt_files",
            "public.fuel_transactions",
            "public.receipt_scan_jobs",
            "public.maintenance_reminders",
            "public.notification_preferences",
            "public.subscription_plans",
            "public.subscriptions",
            "public.payment_transactions",
            "public.payment_webhook_events",
            "public.ai_usage_logs",
            "public.user_feedback"
        )

        for (table in expectedTables) {
            assertTrue("Skema harus mendefinisikan tabel $table", content.contains(table))
        }
    }

    @Test
    fun initialSchema_enforcesCompositeForeignKeysAndConstraints() {
        val schemaFile = File(migrationDir, "20261029000001_initial_schema.sql")
        val content = schemaFile.readText()

        assertTrue(content.contains("foreign key (vehicle_id, user_id)"))
        assertTrue(content.contains("foreign key (receipt_file_id, user_id)"))
        assertTrue(content.contains("pro_monthly"))
        assertTrue(content.contains("pro_yearly"))
    }

    @Test
    fun rlsAndStorage_enablesRlsOnAllThirteenTables() {
        val rlsFile = File(migrationDir, "20261029000002_rls_and_storage.sql")
        val content = rlsFile.readText()

        val expectedTables = listOf(
            "public.profiles",
            "public.vehicles",
            "public.receipt_files",
            "public.fuel_transactions",
            "public.receipt_scan_jobs",
            "public.maintenance_reminders",
            "public.notification_preferences",
            "public.subscription_plans",
            "public.subscriptions",
            "public.payment_transactions",
            "public.payment_webhook_events",
            "public.ai_usage_logs",
            "public.user_feedback"
        )

        for (table in expectedTables) {
            assertTrue("Tabel $table harus diaktifkan RLS", content.contains("alter table $table enable row level security;"))
        }
    }

    @Test
    fun rlsAndStorage_configuresPrivateReceiptStorageBucket() {
        val rlsFile = File(migrationDir, "20261029000002_rls_and_storage.sql")
        val content = rlsFile.readText()

        assertTrue("Storage bucket 'receipts' harus ada", content.contains("'receipts'"))
        assertTrue("Storage bucket harus private", content.contains("false, -- Wajib privat"))
        assertTrue("Harus ada storage policy upload", content.contains("receipts_storage_upload_own"))
        assertTrue("Harus ada storage policy select", content.contains("receipts_storage_select_own"))
        assertTrue("Harus ada storage policy delete", content.contains("receipts_storage_delete_own"))
    }

    @Test
    fun migrationTahap6_enforcesEntitlementsAndQuota() {
        val file = File(migrationDir, "20261029000006_subscription_entitlements_and_quota.sql")
        val content = file.readText()

        assertTrue("Harus menambahkan fitur pada subscription_plans", content.contains("features jsonb not null"))
        assertTrue("Harus men-seed paket free", content.contains("'free'"))
        assertTrue("Harus men-seed paket pro_monthly", content.contains("'pro_monthly'"))
        assertTrue("Harus men-seed paket pro_yearly", content.contains("'pro_yearly'"))
        assertTrue("Harus ada fungsi get_user_effective_entitlement", content.contains("get_user_effective_entitlement"))
        assertTrue("Harus ada fungsi atomik reserve_ai_quota", content.contains("reserve_ai_quota"))
        assertTrue("Harus ada fungsi atomik finalize_ai_quota", content.contains("finalize_ai_quota"))
        assertTrue("Harus ada trigger check_vehicle_limit", content.contains("trg_enforce_vehicle_limit"))
        assertTrue("Fungsi test_set_user_subscription harus dicabut dari publik", content.contains("revoke execute on function public.test_set_user_subscription"))
        assertTrue("Fungsi test_set_user_subscription hanya boleh diakses service_role", content.contains("grant execute on function public.test_set_user_subscription(uuid, text, integer, text) to service_role;"))
    }

    @Test
    fun migrationTahap7_configuresPaymentAndActivation() {
        val file = File(migrationDir, "20261029000007_payment_integration_and_activation.sql")
        val content = file.readText()

        assertTrue("Harus memperluas provider check", content.contains("check (provider in ('google_play', 'qrispy', 'midtrans', 'xendit', 'sandbox'))"))
        assertTrue("Harus menambahkan product_id", content.contains("product_id text"))
        assertTrue("Harus menambahkan base_plan_id", content.contains("base_plan_id text"))
        assertTrue("Harus menambahkan last_payment_transaction_id", content.contains("last_payment_transaction_id uuid"))
        assertTrue("Harus ada fungsi activate_subscription_for_payment", content.contains("activate_subscription_for_payment"))
        assertTrue("Harus ada fungsi handle_subscription_lifecycle_event", content.contains("handle_subscription_lifecycle_event"))
        assertTrue("Harus ada fungsi record_payment_webhook_event", content.contains("record_payment_webhook_event"))
    }

    @Test
    fun migrationTahap8_hardensSecurityAndRevokesPublicPrivileges() {
        val file = File(migrationDir, "20261029000008_security_hardening_and_integrity.sql")
        val content = file.readText()

        assertTrue("Harus me-revoke activate_subscription_for_payment dari public", content.contains("revoke execute on function public.activate_subscription_for_payment"))
        assertTrue("Harus me-revoke handle_subscription_lifecycle_event dari public", content.contains("revoke execute on function public.handle_subscription_lifecycle_event"))
        assertTrue("Harus me-revoke record_payment_webhook_event dari public", content.contains("revoke execute on function public.record_payment_webhook_event"))
        assertTrue("Harus me-revoke reserve_ai_quota dari public", content.contains("revoke execute on function public.reserve_ai_quota"))
        assertTrue("Harus me-revoke finalize_ai_quota dari public", content.contains("revoke execute on function public.finalize_ai_quota"))
        assertTrue("Harus ada service_role guard pada activate_subscription", content.contains("auth.role() != 'service_role'"))
        assertTrue("Harus ada proteksi re-aktivasi status paid", content.contains("v_tx.status = 'paid'"))
    }

    @Test
    fun migrationTahap10_configuresSaasOwnerAndAdminDashboard() {
        val file = File(migrationDir, "20261030000002_saas_admin_owner_dashboard.sql")
        val content = file.readText()

        assertTrue("Harus menambahkan kolom role pada profiles", content.contains("add column if not exists role text not null default 'user'"))
        assertTrue("Harus memeriksa role user, admin, owner", content.contains("check (role in ('user', 'admin', 'owner'))"))
        assertTrue("Harus ada fungsi is_admin_or_owner", content.contains("create or replace function public.is_admin_or_owner"))
        assertTrue("Harus ada RPC get_saas_owner_metrics", content.contains("create or replace function public.get_saas_owner_metrics"))
        assertTrue("Harus ada RPC admin_grant_user_pro", content.contains("create or replace function public.admin_grant_user_pro"))
        assertTrue("Harus ada guard akses owner", content.contains("public.is_admin_or_owner(v_caller_id)"))
    }

    @Test
    fun migrationTahap11_configuresSaasOwnerPlanAndTxManagement() {
        val file = File(migrationDir, "20261030000003_saas_owner_plan_and_tx_management.sql")
        val content = file.readText()

        assertTrue("Harus ada RPC admin_update_subscription_plan", content.contains("create or replace function public.admin_update_subscription_plan"))
        assertTrue("Harus ada RPC admin_get_payment_transactions", content.contains("create or replace function public.admin_get_payment_transactions"))
        assertTrue("Harus ada guard akses is_admin_or_owner", content.contains("public.is_admin_or_owner(v_caller_id)"))
        assertTrue("Harus ada validasi harga negatif", content.contains("p_price_idr < 0"))
        assertTrue("Harus ada validasi kuota negatif", content.contains("p_ai_scan_quota < 0"))
        assertTrue("Harus ada grant execute admin_get_payment_transactions", content.contains("grant execute on function public.admin_get_payment_transactions"))
    }
}
