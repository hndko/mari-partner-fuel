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

        assertTrue("File initial_schema.sql harus ada", schemaFile.exists())
        assertTrue("File rls_and_storage.sql harus ada", rlsFile.exists())
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
