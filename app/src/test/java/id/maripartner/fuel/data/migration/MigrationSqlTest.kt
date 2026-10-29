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

        assertTrue("File initial_schema.sql harus ada", schemaFile.exists())
        assertTrue("File rls_and_storage.sql harus ada", rlsFile.exists())
        assertTrue("File vehicles_and_transactions_tahap3.sql harus ada", tahap3File.exists())
        assertTrue("File receipt_scan_jobs_policy.sql harus ada", tahap4File.exists())
        assertTrue("File maintenance_reminders_enhancement.sql harus ada", tahap5File.exists())
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
}
