package id.maripartner.fuel.feature.owner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maripartner.fuel.core.designsystem.component.FuelTopBar
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.feature.owner.dashboard.OwnerDashboardContent
import id.maripartner.fuel.feature.owner.navigation.OwnerBottomBar
import id.maripartner.fuel.feature.owner.navigation.OwnerTab
import id.maripartner.fuel.feature.owner.plans.EditPlanDialog
import id.maripartner.fuel.feature.owner.plans.OwnerPlansContent
import id.maripartner.fuel.feature.owner.users.GrantProDialog
import id.maripartner.fuel.feature.owner.users.OwnerUsersContent
import id.maripartner.fuel.feature.owner.transactions.OwnerTransactionsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerConsoleScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerConsoleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val error = uiState.errorMessage
        val success = uiState.successMessage
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(OwnerConsoleUiEvent.DismissMessages)
        } else if (!success.isNullOrBlank()) {
            snackbarHostState.showSnackbar(success)
            viewModel.onEvent(OwnerConsoleUiEvent.DismissMessages)
        }
    }

    val topBarTitle = when (uiState.currentTab) {
        OwnerTab.DASHBOARD -> "Panel Eksekutif SaaS"
        OwnerTab.PLANS -> "Katalog & Harga Paket"
        OwnerTab.USERS -> "Pengguna & Akses Pro"
        OwnerTab.TRANSACTIONS -> "Audit Transaksi Masuk"
    }

    Scaffold(
        topBar = {
            FuelTopBar(
                title = topBarTitle,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(OwnerConsoleUiEvent.Refresh) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MariPrimaryBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Segarkan Data",
                                tint = MariPrimaryBlue
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            OwnerBottomBar(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.onEvent(OwnerConsoleUiEvent.SelectTab(it)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (uiState.isLoading && !uiState.isRefreshing) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MariPrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memuat data konsol SaaS...")
                }
            }
        } else {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "OwnerTabTransition"
            ) { tab ->
                when (tab) {
                    OwnerTab.DASHBOARD -> {
                        OwnerDashboardContent(
                            metrics = uiState.metrics,
                            selectedTimeframe = uiState.selectedTimeframe,
                            onTimeframeChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeTimeframe(it)) }
                        )
                    }
                    OwnerTab.PLANS -> {
                        OwnerPlansContent(
                            plans = uiState.plans,
                            onEditPlanClicked = { viewModel.onEvent(OwnerConsoleUiEvent.OpenEditPlanDialog(it)) }
                        )
                    }
                    OwnerTab.USERS -> {
                        OwnerUsersContent(
                            users = uiState.filteredUsers,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChanged = { viewModel.onEvent(OwnerConsoleUiEvent.UpdateSearchQuery(it)) },
                            onGrantProClicked = { viewModel.onEvent(OwnerConsoleUiEvent.OpenGrantProDialog(it)) }
                        )
                    }
                    OwnerTab.TRANSACTIONS -> {
                        OwnerTransactionsContent(
                            transactions = uiState.transactions
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog: Edit Paket & Harga
    val planToEdit = uiState.selectedPlanForEdit
    if (planToEdit != null) {
        EditPlanDialog(
            plan = planToEdit,
            name = uiState.editPlanName,
            priceIdr = uiState.editPlanPriceIdr,
            aiQuota = uiState.editPlanAiQuota,
            maxVehicles = uiState.editPlanMaxVehicles,
            isActive = uiState.editPlanIsActive,
            description = uiState.editPlanDescription,
            isSubmitting = uiState.isSubmittingPlanEdit,
            onNameChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanName(it)) },
            onPriceChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanPrice(it)) },
            onAiQuotaChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanAiQuota(it)) },
            onMaxVehiclesChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanMaxVehicles(it)) },
            onIsActiveChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanIsActive(it)) },
            onDescriptionChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeEditPlanDescription(it)) },
            onDismiss = { viewModel.onEvent(OwnerConsoleUiEvent.DismissEditPlanDialog) },
            onConfirm = { viewModel.onEvent(OwnerConsoleUiEvent.SubmitEditPlan) }
        )
    }

    // Modal Dialog: Berikan / Perpanjang Pro Pengguna
    val userToGrant = uiState.selectedUserForGrantPro
    if (userToGrant != null) {
        GrantProDialog(
            targetUser = userToGrant,
            selectedDays = uiState.grantDurationDays,
            notes = uiState.grantNotes,
            isSubmitting = uiState.isSubmittingGrant,
            onDaysChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeGrantDuration(it)) },
            onNotesChanged = { viewModel.onEvent(OwnerConsoleUiEvent.ChangeGrantNotes(it)) },
            onDismiss = { viewModel.onEvent(OwnerConsoleUiEvent.DismissGrantProDialog) },
            onConfirm = { viewModel.onEvent(OwnerConsoleUiEvent.SubmitGrantPro) }
        )
    }
}
