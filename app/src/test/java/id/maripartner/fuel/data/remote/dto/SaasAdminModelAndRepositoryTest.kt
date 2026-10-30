package id.maripartner.fuel.data.remote.dto

import id.maripartner.fuel.core.common.result.AppResult
import id.maripartner.fuel.domain.model.AdminUserSummary
import id.maripartner.fuel.domain.model.DailyGrowthPoint
import id.maripartner.fuel.domain.model.GrantProResult
import id.maripartner.fuel.domain.model.PlanConversionStats
import id.maripartner.fuel.domain.model.SaasAdminMetrics
import id.maripartner.fuel.domain.model.SaasKpi
import id.maripartner.fuel.domain.model.UserProfile
import id.maripartner.fuel.domain.repository.SaasAdminRepository
import id.maripartner.fuel.feature.admin.SaasAdminUiEvent
import id.maripartner.fuel.feature.admin.SaasAdminViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaasAdminModelAndRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun userProfile_isOwnerOrAdmin_evaluatesCorrectly() {
        val ownerProfile = UserProfile(id = "user-1", role = "owner")
        val adminProfile = UserProfile(id = "user-2", role = "admin")
        val normalProfile = UserProfile(id = "user-3", role = "user")
        val uppercaseOwner = UserProfile(id = "user-4", role = "OWNER")

        assertTrue(ownerProfile.isOwnerOrAdmin)
        assertTrue(adminProfile.isOwnerOrAdmin)
        assertFalse(normalProfile.isOwnerOrAdmin)
        assertTrue(uppercaseOwner.isOwnerOrAdmin)
    }

    @Test
    fun saasAdminMetrics_jsonSerializationAndDeserialization_succeeds() {
        val rawJson = """
            {
                "success": true,
                "timeframe": "30d",
                "kpi": {
                    "total_users": 150,
                    "active_pro_users": 35,
                    "free_users": 115,
                    "total_revenue_idr": 1250000,
                    "total_transactions_recorded": 420,
                    "total_vehicles_registered": 98
                },
                "growth_trend": [
                    { "date_label": "2026-10-28", "count": 12 },
                    { "date_label": "2026-10-29", "count": 18 }
                ],
                "conversion_stats": {
                    "free_users": 115,
                    "pro_monthly_users": 25,
                    "pro_yearly_users": 10,
                    "total_pro_users": 35,
                    "conversion_rate_percent": 23.3
                },
                "recent_users": [
                    {
                        "id": "u-123",
                        "display_name": "Budi Santoso",
                        "created_at": "2026-10-29T10:00:00Z",
                        "role": "user",
                        "is_pro": true,
                        "plan_name": "Mari Pro Bulanan",
                        "expires_at": "2026-11-29T10:00:00Z",
                        "vehicles_count": 2,
                        "transactions_count": 8
                    }
                ],
                "generated_at": "2026-10-30T10:00:00Z"
            }
        """.trimIndent()

        val metrics = json.decodeFromString<SaasAdminMetrics>(rawJson)

        assertTrue(metrics.success)
        assertEquals("30d", metrics.timeframe)
        assertEquals(150L, metrics.kpi.totalUsers)
        assertEquals(35L, metrics.kpi.activeProUsers)
        assertEquals(1250000L, metrics.kpi.totalRevenueIdr)
        assertEquals(2, metrics.growthTrend.size)
        assertEquals("2026-10-28", metrics.growthTrend[0].dateLabel)
        assertEquals(12L, metrics.growthTrend[0].count)
        assertEquals(23.3, metrics.conversionStats.conversionRatePercent, 0.01)
        assertEquals(1, metrics.recentUsers.size)
        assertEquals("Budi Santoso", metrics.recentUsers[0].displayName)
        assertTrue(metrics.recentUsers[0].isPro)
    }

    @Test
    fun saasAdminViewModel_loadsMetricsAndHandlesEventsCorrectly() = runTest {
        val fakeRepo = object : SaasAdminRepository {
            override suspend fun getSaasMetrics(timeframe: String): AppResult<SaasAdminMetrics> {
                return AppResult.Success(
                    SaasAdminMetrics(
                        timeframe = timeframe,
                        kpi = SaasKpi(totalUsers = 50, activeProUsers = 10, totalRevenueIdr = 300000),
                        recentUsers = listOf(
                            AdminUserSummary(id = "user-1", displayName = "John Doe", isPro = false),
                            AdminUserSummary(id = "user-2", displayName = "Jane Smith", isPro = true)
                        )
                    )
                )
            }

            override suspend fun grantUserPro(
                targetUserId: String,
                durationDays: Int,
                notes: String?
            ): AppResult<GrantProResult> {
                return AppResult.Success(
                    GrantProResult(
                        success = true,
                        targetUserId = targetUserId,
                        durationDaysAdded = durationDays,
                        message = "Masa aktif Pro berhasil diperpanjang."
                    )
                )
            }
        }

        val viewModel = SaasAdminViewModel(fakeRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(50L, state.metrics.kpi.totalUsers)
        assertEquals(2, state.filteredRecentUsers.size)

        // Filter search
        viewModel.onEvent(SaasAdminUiEvent.UpdateSearchQuery("Jane"))
        assertEquals(1, viewModel.uiState.value.filteredRecentUsers.size)
        assertEquals("Jane Smith", viewModel.uiState.value.filteredRecentUsers[0].displayName)

        // Open grant pro dialog
        val target = viewModel.uiState.value.filteredRecentUsers[0]
        viewModel.onEvent(SaasAdminUiEvent.OpenGrantProDialog(target))
        assertEquals(target, viewModel.uiState.value.selectedUserForGrantPro)
        assertEquals(30, viewModel.uiState.value.grantDurationDays)

        // Change duration & notes
        viewModel.onEvent(SaasAdminUiEvent.ChangeGrantDuration(90))
        viewModel.onEvent(SaasAdminUiEvent.ChangeGrantNotes("Hadiah Loyalitas"))
        assertEquals(90, viewModel.uiState.value.grantDurationDays)
        assertEquals("Hadiah Loyalitas", viewModel.uiState.value.grantNotes)

        // Submit grant pro
        viewModel.onEvent(SaasAdminUiEvent.SubmitGrantPro)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedUserForGrantPro)
        assertNotNull(viewModel.uiState.value.successMessage)
    }
}
