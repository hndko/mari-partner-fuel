package id.maripartner.fuel.feature.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.maripartner.fuel.core.designsystem.component.FuelEmptyState
import id.maripartner.fuel.core.designsystem.component.FuelTopBar

@Composable
fun TransactionsScreen(
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            FuelTopBar(title = "Riwayat Pengeluaran BBM")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FuelEmptyState(
                title = "Riwayat Transaksi Masih Kosong",
                subtitle = "Semua catatan pengisian bahan bakar Anda akan tersimpan rapi dan dapat dicari di sini.",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                actionButtonText = "Tambah Pengeluaran BBM",
                onActionClick = onNavigateToCreate
            )
        }
    }
}
