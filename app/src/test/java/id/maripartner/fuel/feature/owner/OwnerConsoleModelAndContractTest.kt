package id.maripartner.fuel.feature.owner

import id.maripartner.fuel.domain.model.owner.AdminUserSummary
import id.maripartner.fuel.domain.model.owner.DailyGrowthPoint
import id.maripartner.fuel.domain.model.owner.GrantProResult
import id.maripartner.fuel.domain.model.owner.OwnerPaymentTx
import id.maripartner.fuel.domain.model.owner.OwnerPlanItem
import id.maripartner.fuel.domain.model.owner.PlanConversionStats
import id.maripartner.fuel.domain.model.owner.SaasAdminMetrics
import id.maripartner.fuel.domain.model.owner.SaasKpi
import id.maripartner.fuel.feature.owner.navigation.OwnerTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerConsoleModelAndContractTest {

    @Test
    fun ownerTab_enumHasFourDistinctTabs() {
        assertEquals(4, OwnerTab.entries.size)
        assertEquals(OwnerTab.DASHBOARD, OwnerTab.entries[0])
        assertEquals(OwnerTab.PLANS, OwnerTab.entries[1])
        assertEquals(OwnerTab.USERS, OwnerTab.entries[2])
        assertEquals(OwnerTab.TRANSACTIONS, OwnerTab.entries[3])
    }

    @Test
    fun ownerConsoleUiState_defaultValuesAreClean() {
        val state = OwnerConsoleUiState()
        assertEquals(OwnerTab.DASHBOARD, state.currentTab)
        assertEquals("30d", state.selectedTimeframe)
        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals("", state.searchQuery)
        assertTrue(state.plans.isEmpty())
        assertTrue(state.transactions.isEmpty())
        assertTrue(state.filteredUsers.isEmpty())
    }

    @Test
    fun ownerConsoleUiState_filteredUsersWorksAccurately() {
        val user1 = AdminUserSummary(
            id = "u1-aaa",
            displayName = "Budi Hartono",
            role = "user",
            planName = "Free Tier",
            isPro = false,
            vehiclesCount = 1,
            transactionsCount = 5,
            createdAt = "2026-10-01"
        )
        val user2 = AdminUserSummary(
            id = "u2-bbb",
            displayName = "Siti Aisyah",
            role = "user",
            planName = "Paket Pro Bulanan",
            isPro = true,
            vehiclesCount = 3,
            transactionsCount = 20,
            createdAt = "2026-10-05"
        )

        val metrics = SaasAdminMetrics(recentUsers = listOf(user1, user2))
        val state = OwnerConsoleUiState(metrics = metrics, isLoading = false)

        // Tanpa filter -> return all
        assertEquals(2, state.filteredUsers.size)

        // Filter nama "budi"
        val stateBudi = state.copy(searchQuery = "budi")
        assertEquals(1, stateBudi.filteredUsers.size)
        assertEquals("Budi Hartono", stateBudi.filteredUsers.first().displayName)

        // Filter status "pro"
        val statePro = state.copy(searchQuery = "pro")
        assertEquals(1, statePro.filteredUsers.size)
        assertEquals("Siti Aisyah", statePro.filteredUsers.first().displayName)

        // Filter no match
        val stateNone = state.copy(searchQuery = "nonexistent")
        assertTrue(stateNone.filteredUsers.isEmpty())
    }

    @Test
    fun ownerPlanItem_modelPreservesAllAttributes() {
        val plan = OwnerPlanItem(
            id = "plan-1",
            code = "pro_monthly",
            name = "Paket Pro Bulanan",
            billingPeriod = "month",
            priceIdr = 29000L,
            aiScanQuota = 100,
            maxVehicles = null,
            isActive = true,
            description = "Akses tak terbatas untuk operasional armada"
        )

        assertEquals("plan-1", plan.id)
        assertEquals("pro_monthly", plan.code)
        assertEquals(29000L, plan.priceIdr)
        assertEquals(100, plan.aiScanQuota)
        assertEquals(null, plan.maxVehicles)
        assertTrue(plan.isActive)
    }

    @Test
    fun ownerPaymentTx_modelPreservesAttributes() {
        val tx = OwnerPaymentTx(
            id = "tx-100",
            userId = "user-abc",
            userDisplayName = "Handoko",
            provider = "qrispy",
            paymentReference = "QRIS-88219",
            amountIdr = 29000L,
            status = "paid",
            createdAt = "2026-10-30T10:00:00Z",
            paidAt = "2026-10-30T10:02:15Z",
            planName = "Paket Pro Bulanan"
        )

        assertEquals("tx-100", tx.id)
        assertEquals("Handoko", tx.userDisplayName)
        assertEquals(29000L, tx.amountIdr)
        assertEquals("paid", tx.status)
        assertEquals("QRIS-88219", tx.paymentReference)
        assertNotNull(tx.paidAt)
    }

    @Test
    fun ownerPaymentTx_statusAndAmountAggregationCalculation() {
        val txList = listOf(
            OwnerPaymentTx(
                id = "tx-1",
                userId = "u-1",
                userDisplayName = "Handoko",
                provider = "qrispy",
                paymentReference = "QRIS-001",
                amountIdr = 29000L,
                status = "paid",
                planName = "Paket Pro Bulanan"
            ),
            OwnerPaymentTx(
                id = "tx-2",
                userId = "u-2",
                userDisplayName = "Budi Hartono",
                provider = "qrispy",
                paymentReference = "QRIS-002",
                amountIdr = 290000L,
                status = "paid",
                planName = "Paket Pro Tahunan"
            ),
            OwnerPaymentTx(
                id = "tx-3",
                userId = "u-3",
                userDisplayName = "Siti Aisyah",
                provider = "midtrans",
                paymentReference = "MID-003",
                amountIdr = 29000L,
                status = "pending",
                planName = "Paket Pro Bulanan"
            )
        )

        val totalPaidAmount = txList.filter { it.status.equals("paid", ignoreCase = true) }.sumOf { it.amountIdr }
        val totalPendingAmount = txList.filter { it.status.equals("pending", ignoreCase = true) }.sumOf { it.amountIdr }
        val paidCount = txList.count { it.status.equals("paid", ignoreCase = true) }
        val pendingCount = txList.count { it.status.equals("pending", ignoreCase = true) }
        val successRate = (paidCount.toDouble() / txList.size.toDouble()) * 100.0

        assertEquals(319000L, totalPaidAmount)
        assertEquals(29000L, totalPendingAmount)
        assertEquals(2, paidCount)
        assertEquals(1, pendingCount)
        assertEquals(66.67, successRate, 0.01)

        // Filter status "paid"
        val paidOnly = txList.filter { it.status.equals("paid", ignoreCase = true) }
        assertEquals(2, paidOnly.size)

        // Filter status "pending"
        val pendingOnly = txList.filter { it.status.equals("pending", ignoreCase = true) }
        assertEquals(1, pendingOnly.size)

        // Search query "budi"
        val queryBudi = txList.filter { it.userDisplayName.contains("budi", ignoreCase = true) }
        assertEquals(1, queryBudi.size)
        assertEquals("Budi Hartono", queryBudi.first().userDisplayName)
    }

    @Test
    fun saasKpiAndMetrics_modelCalculations() {
        val kpi = SaasKpi(
            totalRevenueIdr = 580000L,
            totalUsers = 120L,
            activeProUsers = 20L,
            totalTransactionsRecorded = 450L
        )

        assertEquals(580000L, kpi.totalRevenueIdr)
        assertEquals(120L, kpi.totalUsers)
        assertEquals(20L, kpi.activeProUsers)
        assertEquals(450L, kpi.totalTransactionsRecorded)

        val point = DailyGrowthPoint(dateLabel = "2026-10-29", count = 5L)
        assertEquals("2026-10-29", point.dateLabel)
        assertEquals(5L, point.count)

        val conv = PlanConversionStats(
            freeUsers = 100L,
            proMonthlyUsers = 15L,
            proYearlyUsers = 5L,
            totalProUsers = 20L,
            conversionRatePercent = 16.67
        )
        assertEquals(100L, conv.freeUsers)
        assertEquals(20L, conv.totalProUsers)
        assertEquals(16.67, conv.conversionRatePercent, 0.001)

        val grantRes = GrantProResult(
            success = true,
            isExtended = false,
            targetUserId = "user-1",
            durationDaysAdded = 30,
            message = "Pro berhasil diaktifkan"
        )
        assertTrue(grantRes.success)
        assertFalse(grantRes.isExtended)
        assertEquals("user-1", grantRes.targetUserId)
        assertEquals(30, grantRes.durationDaysAdded)
    }
}
