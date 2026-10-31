package id.maripartner.fuel.feature.owner.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.core.designsystem.component.FuelChipType
import id.maripartner.fuel.core.designsystem.component.FuelStatusChip
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import id.maripartner.fuel.domain.model.owner.AdminUserSummary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerUsersContent(
    users: List<AdminUserSummary>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onGrantProClicked: (AdminUserSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedTierFilter by remember { mutableStateOf("all") } // "all", "pro", "free"

    val proCount = users.count { it.isPro }
    val freeCount = users.count { !it.isPro }

    val filteredUsers = remember(users, selectedTierFilter) {
        when (selectedTierFilter) {
            "pro" -> users.filter { it.isPro }
            "free" -> users.filter { !it.isPro }
            else -> users
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Banner Header
        item {
            UsersHeaderBanner(
                totalUsers = users.size,
                proUsers = proCount
            )
        }

        // 2. Summary KPI Strip
        item {
            UsersKpiSummaryStrip(
                totalUsers = users.size,
                proUsers = proCount,
                freeUsers = freeCount
            )
        }

        // 3. Search Bar & Tier Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Cari nama, ID pengguna, atau status...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Pengguna",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearchQueryChanged("")
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Pencarian"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter Chips (Semua, Pro, Free)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedTierFilter == "all",
                        onClick = { selectedTierFilter = "all" },
                        label = {
                            Text(
                                text = "Semua (${users.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTierFilter == "all") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MariPrimaryBlue,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = selectedTierFilter == "pro",
                        onClick = { selectedTierFilter = "pro" },
                        label = {
                            Text(
                                text = "Pro (${proCount})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTierFilter == "pro") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = selectedTierFilter == "free",
                        onClick = { selectedTierFilter = "free" },
                        label = {
                            Text(
                                text = "Free (${freeCount})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTierFilter == "free") FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF64748B),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // 4. Section Label & Result Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Pengguna SaaS (${filteredUsers.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (searchQuery.isNotBlank() || selectedTierFilter != "all") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MariPrimaryBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Filter Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                color = MariPrimaryBlue
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 5. User Cards or Empty State
        if (filteredUsers.isEmpty()) {
            item {
                EmptyUsersCard(isSearch = searchQuery.isNotBlank() || selectedTierFilter != "all")
            }
        } else {
            items(filteredUsers, key = { it.id }) { user ->
                UserItemCard(
                    user = user,
                    onGrantProClicked = {
                        focusManager.clearFocus()
                        onGrantProClicked(user)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UsersHeaderBanner(
    totalUsers: Int,
    proUsers: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A), // Dark Slate 900 VIP Executive
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MANAJEMEN AKUN & AKSES PRO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.5.sp
                        ),
                        color = Color(0xFFFBBF24)
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "DIRECTORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color(0xFF34D399)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pengguna & Otoritas Akses SaaS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pantau status paket pengguna, armada terdaftar, dan berikan akses Pro manual secara instan.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
            }
        }
    }
}

@Composable
private fun UsersKpiSummaryStrip(
    totalUsers: Int,
    proUsers: Int,
    freeUsers: Int
) {
    val conversionPct = if (totalUsers > 0) {
        String.format(Locale.US, "%.1f", (proUsers.toDouble() / totalUsers) * 100)
    } else {
        "0.0"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UsersSummaryTile(
            label = "TOTAL AKUN",
            value = "$totalUsers User",
            subtext = "Terdaftar di SaaS",
            accentColor = MariPrimaryBlue,
            modifier = Modifier.weight(1f)
        )
        UsersSummaryTile(
            label = "PRO BERBAYAR",
            value = "$proUsers Akun",
            subtext = "$conversionPct% Konversi",
            accentColor = Color(0xFFD97706),
            modifier = Modifier.weight(1f)
        )
        UsersSummaryTile(
            label = "FREE TIER",
            value = "$freeUsers Akun",
            subtext = "Starter Aktif",
            accentColor = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UsersSummaryTile(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    color = accentColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UserItemCard(
    user: AdminUserSummary,
    onGrantProClicked: () -> Unit
) {
    val isOwner = user.role.equals("owner", ignoreCase = true)
    val isAdmin = user.role.equals("admin", ignoreCase = true)

    val cardBorder = when {
        user.isPro -> BorderStroke(1.2.dp, Color(0xFFD97706).copy(alpha = 0.45f))
        isOwner -> BorderStroke(1.2.dp, MariPrimaryBlue.copy(alpha = 0.45f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (user.isPro) 2.5.dp else 1.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar Skuirkel + Nama + Role Badges + Pro Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inisial Avatar Squircle
                val initial = user.displayName.take(1).uppercase().ifBlank { "U" }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (user.isPro) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (user.isPro) Color(0xFFD97706).copy(alpha = 0.35f) else Color.Transparent
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (user.isPro) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isOwner) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "👑 OWNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFFB45309)
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isAdmin) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "🛡️ ADMIN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF0369A1)
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "UID: ${user.id.take(8)}...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = "• ${user.planName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (user.isPro) MariPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge status Pro / Free
                if (user.isPro) {
                    FuelStatusChip(
                        text = "PRO",
                        type = FuelChipType.Success,
                        icon = Icons.Default.Stars
                    )
                } else {
                    FuelStatusChip(
                        text = "FREE",
                        type = FuelChipType.Info
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics Micro-Tiles: Armada & Transaksi BBM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Box 1: Armada Terhubung
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MariPrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${user.vehiclesCount} Kendaraan",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Box 2: Transaksi BBM
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${user.transactionsCount} Transaksi BBM",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expiry info if active Pro
            val expiry = user.expiresAt
            if (user.isPro) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (expiry.isNullOrBlank()) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (expiry.isNullOrBlank()) {
                            "Masa aktif Pro: Permanen (Seumur Hidup)"
                        } else {
                            "Masa aktif Pro s/d: ${DateUtils.formatIsoDate(expiry)}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (expiry.isNullOrBlank()) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Berikan Akses Pro / Perpanjang Pro
            if (user.isPro) {
                Button(
                    onClick = onGrantProClicked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFF92400E)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perpanjang Masa Aktif Pro",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    )
                }
            } else {
                Button(
                    onClick = onGrantProClicked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Berikan Akses Pro Manual",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyUsersCard(isSearch: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isSearch) "Pengguna Tidak Ditemukan" else "Belum Ada Pengguna Terdaftar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSearch) {
                    "Tidak ada akun pengguna yang cocok dengan kriteria pencarian atau filter yang dipilih."
                } else {
                    "Belum ada riwayat pendaftaran akun pengguna pada basis data."
                },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
