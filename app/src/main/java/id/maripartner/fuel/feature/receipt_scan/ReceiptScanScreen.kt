package id.maripartner.fuel.feature.receipt_scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maripartner.fuel.core.designsystem.component.FuelPrimaryButton
import id.maripartner.fuel.core.designsystem.theme.MariAccentCyan
import id.maripartner.fuel.core.designsystem.theme.MariBrandNavy
import id.maripartner.fuel.core.designsystem.theme.MariMutedLight
import id.maripartner.fuel.core.designsystem.theme.MariPrimaryBlue
import java.io.ByteArrayOutputStream

@Composable
fun ReceiptScanScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    viewModel: ReceiptScanViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Camera Capture Launcher (Preview Bitmap langsung tanpa simpan file fisik sementara)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val bytes = stream.toByteArray()
            viewModel.onEvent(ReceiptScanUiEvent.ImageSelected(bitmap, bytes, "image/jpeg"))
        }
    }

    // PhotoPicker Launcher (Android PhotoPicker resmi tanpa izin berisiko)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    if (bitmap != null) {
                        viewModel.onEvent(ReceiptScanUiEvent.ImageSelected(bitmap, bytes, mime))
                    }
                }
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReceiptScanUiEvent.DismissMessage)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReceiptScanUiEvent.DismissMessage)
        }
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MariBrandNavy,
                MariPrimaryBlue.copy(alpha = 0.95f),
                MariPrimaryBlue
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val screenHeight = maxHeight
            val density = LocalDensity.current
            var headerHeightDp by remember { mutableStateOf(200.dp) }

            // Latar belakang gradien atas untuk mencegah celah saat pull-down overscroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(brush = gradientBrush)
            )

            val remainingCardMinHeight = (screenHeight - headerHeightDp + 36.dp).coerceAtLeast(320.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Hero Gradient Header (Status Bar, Back Button, Title, AI Pill, Guide)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            val measuredDp = with(density) { size.height.toDp() }
                            if (measuredDp > 0.dp) {
                                headerHeightDp = measuredDp
                            }
                        }
                        .background(brush = gradientBrush)
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // Top Bar: Tombol Kembali 44dp & Pill AI Local-First
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Kembali",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Pill Keamanan & Privasi
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ML Kit Local-First",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.size(44.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Judul & Ilustrasi Singkat
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MariPrimaryBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Pindai Struk BBM",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 22.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Deteksi otomatis SPBU, liter, dan nominal tanpa simpan file foto",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Elevated White Rounded Sheet Container (Pilihan Aksi & Panduan)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-18).dp)
                        .defaultMinSize(minHeight = remainingCardMinHeight),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = remainingCardMinHeight),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(
                                    top = 22.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (state.selectedBitmap == null) {
                                // Tampilan Awal: Pilihan Kamera atau Galeri
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(22.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ambil atau Pilih Foto Struk",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Struk SPBU akan dipindai langsung di perangkat secara lokal dengan Google ML Kit Text Recognition.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MariMutedLight,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            FuelPrimaryButton(
                                                text = "Buka Kamera",
                                                onClick = { cameraLauncher.launch(null) },
                                                leadingIcon = Icons.Default.CameraAlt,
                                                modifier = Modifier.weight(1f)
                                            )

                                            OutlinedButton(
                                                onClick = {
                                                    galleryLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, MariPrimaryBlue)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = MariPrimaryBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Pilih Galeri",
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MariPrimaryBlue
                                                )
                                            }
                                        }
                                    }
                                }

                                // Panduan Checklist Foto Struk
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = Color(0xFFEAB308),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Tips Pemindaian Presisi 100%",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✓", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Ratakan kertas struk pada bidang datar dengan pencahayaan terang",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MariMutedLight
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✓", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Pastikan nominal Rupiah, volume liter, dan nama SPBU terlihat tajam",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MariMutedLight
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✓", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Hindari pantulan cahaya flash langsung atau bayangan jari yang menutupi teks",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MariMutedLight
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Tampilan Pratinjau Foto & Status Tahap OCR
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            bitmap = state.selectedBitmap!!.asImageBitmap(),
                                            contentDescription = "Foto Struk",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Status Pemrosesan OCR Nyata
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    when (state.stage) {
                                                        OcrProcessingStage.COMPLETED -> Color(0xFFDCFCE7)
                                                        OcrProcessingStage.FAILED -> Color(0xFFFEE2E2)
                                                        else -> Color(0xFFF1F5F9)
                                                    }
                                                )
                                                .padding(14.dp)
                                        ) {
                                            if (state.isProcessing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MariPrimaryBlue
                                                )
                                            } else if (state.stage == OcrProcessingStage.COMPLETED) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = state.stage.displayLabel,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = when (state.stage) {
                                                    OcrProcessingStage.COMPLETED -> Color(0xFF15803D)
                                                    OcrProcessingStage.FAILED -> Color(0xFFB91C1C)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Aksi Lanjut ke Review atau Ganti Foto
                                        if (state.stage == OcrProcessingStage.COMPLETED) {
                                            FuelPrimaryButton(
                                                text = "Periksa & Tinjau Hasil",
                                                onClick = onNavigateToReview,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.onEvent(ReceiptScanUiEvent.ClearImage) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Ganti Foto")
                                            }

                                            if (state.stage == OcrProcessingStage.FAILED) {
                                                OutlinedButton(
                                                    onClick = { viewModel.onEvent(ReceiptScanUiEvent.RetryLocalOcr) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Ulangi OCR")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
