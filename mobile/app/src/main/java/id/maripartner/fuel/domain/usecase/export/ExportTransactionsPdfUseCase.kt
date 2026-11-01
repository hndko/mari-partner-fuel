package id.maripartner.fuel.domain.usecase.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import id.maripartner.fuel.core.common.util.CurrencyFormatter
import id.maripartner.fuel.core.common.util.DateUtils
import id.maripartner.fuel.domain.model.FuelTransaction
import id.maripartner.fuel.domain.model.Vehicle
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.util.Locale

/**
 * Use Case untuk menghasilkan dokumen Laporan Keuangan Pengeluaran BBM resmi (Format PDF)
 * ber-kop korporat, ringkasan eksekutif, tabel transaksi rinci, dan kolom pengesahan tanda tangan.
 * Berjalan murni di perangkat lokal (on-device) menggunakan Android Native PdfDocument tanpa API berbayar.
 */
class ExportTransactionsPdfUseCase {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
        private const val PAGE_HEIGHT = 842 // A4 standard height in points (72 dpi)
        private const val MARGIN = 36f
        private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2) // 523 pt
    }

    /**
     * Menghasilkan dokumen PDF dan menyimpannya ke berkas File tujuan.
     */
    fun exportToPdfFile(
        transactions: List<FuelTransaction>,
        vehiclesMap: Map<String, Vehicle>,
        targetFile: File,
        driverName: String = "Pemilik Akun",
        companyOrDept: String = "Klaim Operasional Mandiri"
    ): File {
        targetFile.parentFile?.mkdirs()
        FileOutputStream(targetFile).use { out ->
            exportToPdfStream(transactions, vehiclesMap, out, driverName, companyOrDept)
        }
        return targetFile
    }

    /**
     * Menulis byte PDF ke OutputStream (misal berkas lokal atau DocumentUri SAF).
     */
    fun exportToPdfStream(
        transactions: List<FuelTransaction>,
        vehiclesMap: Map<String, Vehicle>,
        outputStream: OutputStream,
        driverName: String = "Pemilik Akun",
        companyOrDept: String = "Klaim Operasional Mandiri"
    ) {
        val document = PdfDocument()

        // Setup Paints
        val brandPaint = Paint().apply {
            color = Color.rgb(0, 77, 64) // Petrol Teal Primary
            isAntiAlias = true
        }
        val accentPaint = Paint().apply {
            color = Color.rgb(217, 119, 6) // Fuel Amber
            isAntiAlias = true
        }
        val textTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textSubtitlePaint = Paint().apply {
            color = Color.rgb(204, 251, 241) // Light teal text
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val textBodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 8.5f
            isAntiAlias = true
        }
        val textBoldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textMutedPaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate 500
            textSize = 7.5f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200 border
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val rowAltBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val totalHighlightBgPaint = Paint().apply {
            color = Color.rgb(236, 253, 245) // Emerald 50
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Hitung Agregasi Keuangan
        val totalAmount = transactions.sumOf { it.totalAmountIdr }
        val totalVolume = transactions.mapNotNull { it.volumeLiters }.sum()
        val avgPricePerLiter = if (totalVolume > 0.0) (totalAmount / totalVolume).toLong() else 0L
        val docNumber = "MPF-EXP-${LocalDate.now().year}${"%02d".format(LocalDate.now().monthValue)}-${(1000..9999).random()}"
        val printDate = DateUtils.formatIsoDate(LocalDate.now().toString())

        // Pagination Setup
        val rowsPerPageFirst = 14
        val rowsPerPageSubsequent = 22
        val totalPages = calculateTotalPages(transactions.size, rowsPerPageFirst, rowsPerPageSubsequent)

        var txIndex = 0
        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = MARGIN

            if (pageIndex == 1) {
                // 1. Header Ber-Kop Resmi
                val headerRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 62f)
                canvas.drawRoundRect(headerRect, 8f, 8f, brandPaint)

                canvas.drawText("MARI PARTNER FUEL — PRO ENTERPRISE", MARGIN + 14f, currentY + 22f, textTitlePaint)
                canvas.drawText(
                    "LAPORAN RESMI KLAIM & AUDIT PENGELUARAN BAHAN BAKAR (REIMBURSEMENT / PAJAK)",
                    MARGIN + 14f,
                    currentY + 36f,
                    textSubtitlePaint
                )
                canvas.drawText(
                    "Sistem Informasi Pengeluaran Bahan Bakar Terverifikasi Digital • Dokumen Sah",
                    MARGIN + 14f,
                    currentY + 48f,
                    textSubtitlePaint
                )

                // Badge Resmi di pojok kanan header
                val badgeRect = RectF(MARGIN + CONTENT_WIDTH - 110f, currentY + 12f, MARGIN + CONTENT_WIDTH - 12f, currentY + 30f)
                canvas.drawRoundRect(badgeRect, 4f, 4f, accentPaint)
                val badgeTextPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 7.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("TERVERIFIKASI PRO", MARGIN + CONTENT_WIDTH - 100f, currentY + 24f, badgeTextPaint)

                currentY += 72f

                // 2. Metadata Pemohon & Dokumen (Box Rapi)
                val metaRect = RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 42f)
                canvas.drawRoundRect(metaRect, 6f, 6f, rowAltBgPaint)
                canvas.drawRoundRect(metaRect, 6f, 6f, linePaint)

                // Kolom Kiri
                canvas.drawText("Nomor Dokumen : $docNumber", MARGIN + 10f, currentY + 16f, textBoldPaint)
                canvas.drawText("Nama Pemohon  : $driverName", MARGIN + 10f, currentY + 30f, textBodyPaint)

                // Kolom Kanan
                val midX = MARGIN + (CONTENT_WIDTH / 2f) + 10f
                canvas.drawText("Tanggal Terbit : $printDate", midX, currentY + 16f, textBoldPaint)
                canvas.drawText("Keperluan      : $companyOrDept", midX, currentY + 30f, textBodyPaint)

                currentY += 50f

                // 3. Ringkasan Eksekutif Finansial (4 Kartu Mini)
                val tileWidth = (CONTENT_WIDTH - 18f) / 4f
                val tileHeight = 44f
                val tiles = listOf(
                    "TOTAL BIAYA" to CurrencyFormatter.formatIdr(totalAmount),
                    "TOTAL VOLUME" to CurrencyFormatter.formatLiters(totalVolume, compact = true),
                    "RATA-RATA/L" to CurrencyFormatter.formatIdr(avgPricePerLiter),
                    "PENGISIAN" to "${transactions.size} Transaksi"
                )

                tiles.forEachIndexed { i, (label, value) ->
                    val tileX = MARGIN + (i * (tileWidth + 6f))
                    val tileRect = RectF(tileX, currentY, tileX + tileWidth, currentY + tileHeight)
                    canvas.drawRoundRect(tileRect, 6f, 6f, headerBgPaint)
                    canvas.drawRoundRect(tileRect, 6f, 6f, linePaint)

                    canvas.drawText(label, tileX + 8f, currentY + 16f, textMutedPaint)
                    canvas.drawText(value, tileX + 8f, currentY + 32f, textBoldPaint)
                }

                currentY += 56f
            } else {
                // Header Ringkas untuk Halaman Lanjutan
                canvas.drawText("MARI PARTNER FUEL — Laporan Pengeluaran BBM (Lanjutan)", MARGIN, currentY + 12f, textBoldPaint)
                canvas.drawText("No. Dokumen: $docNumber • Halaman $pageIndex dari $totalPages", MARGIN, currentY + 24f, textMutedPaint)
                canvas.drawLine(MARGIN, currentY + 30f, MARGIN + CONTENT_WIDTH, currentY + 30f, linePaint)
                currentY += 40f
            }

            // 4. Header Kolom Tabel
            val tableY = currentY
            drawTableHeader(canvas, tableY, headerBgPaint, linePaint, textBoldPaint)
            currentY += 20f

            // 5. Baris Transaksi
            val rowsThisPage = if (pageIndex == 1) rowsPerPageFirst else rowsPerPageSubsequent
            var rowsDrawn = 0

            while (txIndex < transactions.size && rowsDrawn < rowsThisPage) {
                val tx = transactions[txIndex]
                val isAlt = txIndex % 2 == 1
                val vehicleName = vehiclesMap[tx.vehicleId]?.let { "${it.name} (${it.licensePlate ?: "-"})" }
                    ?: tx.vehicleName
                    ?: "-"

                drawTableRow(
                    canvas = canvas,
                    y = currentY,
                    index = txIndex + 1,
                    tx = tx,
                    vehicleStr = vehicleName,
                    isAlt = isAlt,
                    altPaint = rowAltBgPaint,
                    linePaint = linePaint,
                    textPaint = textBodyPaint
                )

                currentY += 19f
                txIndex++
                rowsDrawn++
            }

            // Jika halaman terakhir, gambar baris Grand Total & Kolom Pengesahan Tanda Tangan
            if (pageIndex == totalPages) {
                // Baris Grand Total
                drawGrandTotalRow(
                    canvas = canvas,
                    y = currentY,
                    totalLiters = totalVolume,
                    totalAmount = totalAmount,
                    highlightPaint = totalHighlightBgPaint,
                    linePaint = linePaint,
                    boldPaint = textBoldPaint
                )
                currentY += 34f

                // 6. Kolom Pengesahan Tanda Tangan
                val sigBoxWidth = (CONTENT_WIDTH - 20f) / 2f
                val sigHeight = 75f

                // Kotak Kiri: Pemohon
                val leftSigRect = RectF(MARGIN, currentY, MARGIN + sigBoxWidth, currentY + sigHeight)
                canvas.drawRoundRect(leftSigRect, 6f, 6f, rowAltBgPaint)
                canvas.drawRoundRect(leftSigRect, 6f, 6f, linePaint)
                canvas.drawText("Diajukan Oleh (Pemohon):", MARGIN + 10f, currentY + 16f, textMutedPaint)
                canvas.drawLine(MARGIN + 10f, currentY + 54f, MARGIN + sigBoxWidth - 10f, currentY + 54f, linePaint)
                canvas.drawText("( $driverName )", MARGIN + 10f, currentY + 66f, textBoldPaint)

                // Kotak Kanan: Approval Finance / Atasan
                val rightSigX = MARGIN + sigBoxWidth + 20f
                val rightSigRect = RectF(rightSigX, currentY, rightSigX + sigBoxWidth, currentY + sigHeight)
                canvas.drawRoundRect(rightSigRect, 6f, 6f, rowAltBgPaint)
                canvas.drawRoundRect(rightSigRect, 6f, 6f, linePaint)
                canvas.drawText("Diverifikasi & Disetujui Oleh:", rightSigX + 10f, currentY + 16f, textMutedPaint)
                canvas.drawLine(rightSigX + 10f, currentY + 54f, rightSigX + sigBoxWidth - 10f, currentY + 54f, linePaint)
                canvas.drawText("( Atasan / Finance Manager )", rightSigX + 10f, currentY + 66f, textBoldPaint)
            }

            // 7. Footer Resmi Dokumen
            val footerY = PAGE_HEIGHT - MARGIN + 10f
            canvas.drawLine(MARGIN, footerY - 14f, MARGIN + CONTENT_WIDTH, footerY - 14f, linePaint)
            canvas.drawText(
                "Dokumen ini diterbitkan secara otomatis oleh Mari Partner Fuel Pro System • Bukti transaksi sah & terenkripsi.",
                MARGIN,
                footerY,
                textMutedPaint
            )
            canvas.drawText(
                "Halaman $pageIndex dari $totalPages",
                MARGIN + CONTENT_WIDTH - 65f,
                footerY,
                textBoldPaint
            )

            document.finishPage(page)
        }

        document.writeTo(outputStream)
        document.close()
    }

    private fun calculateTotalPages(totalItems: Int, firstPageRows: Int, subsequentRows: Int): Int {
        if (totalItems <= firstPageRows) return 1
        val remaining = totalItems - firstPageRows
        return 1 + kotlin.math.ceil(remaining.toDouble() / subsequentRows.toDouble()).toInt()
    }

    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        bgPaint: Paint,
        linePaint: Paint,
        textPaint: Paint
    ) {
        val rect = RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 20f)
        canvas.drawRect(rect, bgPaint)
        canvas.drawRect(rect, linePaint)

        canvas.drawText("#", MARGIN + 4f, y + 13f, textPaint)
        canvas.drawText("Tanggal", MARGIN + 22f, y + 13f, textPaint)
        canvas.drawText("Armada / Nopol", MARGIN + 80f, y + 13f, textPaint)
        canvas.drawText("SPBU", MARGIN + 195f, y + 13f, textPaint)
        canvas.drawText("BBM", MARGIN + 285f, y + 13f, textPaint)
        canvas.drawText("Volume", MARGIN + 355f, y + 13f, textPaint)
        canvas.drawText("Total (Rp)", MARGIN + 415f, y + 13f, textPaint)
        canvas.drawText("Odo (km)", MARGIN + 475f, y + 13f, textPaint)
    }

    private fun drawTableRow(
        canvas: Canvas,
        y: Float,
        index: Int,
        tx: FuelTransaction,
        vehicleStr: String,
        isAlt: Boolean,
        altPaint: Paint,
        linePaint: Paint,
        textPaint: Paint
    ) {
        val rect = RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 19f)
        if (isAlt) {
            canvas.drawRect(rect, altPaint)
        }
        canvas.drawLine(MARGIN, y + 19f, MARGIN + CONTENT_WIDTH, y + 19f, linePaint)

        val dateStr = tx.transactionAt.toString().take(10)
        val station = (tx.stationName ?: "-").take(16)
        val fuel = (tx.fuelType ?: "-").take(12)
        val volStr = tx.volumeLiters?.let { "%.2f L".format(Locale.US, it) } ?: "-"
        val totalStr = CurrencyFormatter.formatThousands(tx.totalAmountIdr)
        val odoStr = tx.odometerKm?.let { "%,.0f".format(Locale.US, it).replace(',', '.') } ?: "-"

        canvas.drawText("$index", MARGIN + 4f, y + 13f, textPaint)
        canvas.drawText(dateStr, MARGIN + 22f, y + 13f, textPaint)
        canvas.drawText(vehicleStr.take(18), MARGIN + 80f, y + 13f, textPaint)
        canvas.drawText(station, MARGIN + 195f, y + 13f, textPaint)
        canvas.drawText(fuel, MARGIN + 285f, y + 13f, textPaint)
        canvas.drawText(volStr, MARGIN + 355f, y + 13f, textPaint)
        canvas.drawText(totalStr, MARGIN + 415f, y + 13f, textPaint)
        canvas.drawText(odoStr, MARGIN + 475f, y + 13f, textPaint)
    }

    private fun drawGrandTotalRow(
        canvas: Canvas,
        y: Float,
        totalLiters: Double,
        totalAmount: Long,
        highlightPaint: Paint,
        linePaint: Paint,
        boldPaint: Paint
    ) {
        val rect = RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 22f)
        canvas.drawRect(rect, highlightPaint)
        canvas.drawRect(rect, linePaint)

        canvas.drawText("TOTAL KESELURUHAN (GRAND TOTAL):", MARGIN + 80f, y + 15f, boldPaint)
        val volStr = "%.2f L".format(Locale.US, totalLiters)
        val totalStr = CurrencyFormatter.formatIdr(totalAmount)

        canvas.drawText(volStr, MARGIN + 355f, y + 15f, boldPaint)
        canvas.drawText(totalStr, MARGIN + 415f, y + 15f, boldPaint)
    }
}
