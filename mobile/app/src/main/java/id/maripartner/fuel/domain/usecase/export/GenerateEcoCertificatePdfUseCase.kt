package id.maripartner.fuel.domain.usecase.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import id.maripartner.fuel.domain.model.EcoDriverReport
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class GenerateEcoCertificatePdfUseCase {

    companion object {
        // Landscape A4 certificate (842 x 595 points)
        private const val PAGE_WIDTH = 842
        private const val PAGE_HEIGHT = 595
        private const val MARGIN = 40f
    }

    fun generateCertificateFile(
        report: EcoDriverReport,
        targetFile: File
    ): File {
        targetFile.parentFile?.mkdirs()
        FileOutputStream(targetFile).use { out ->
            generateCertificateStream(report, out)
        }
        return targetFile
    }

    fun generateCertificateStream(
        report: EcoDriverReport,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCertificate(canvas, report)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    private fun drawCertificate(canvas: Canvas, report: EcoDriverReport) {
        // 1. Latar Belakang & Bingkai Emas / Hijau Elegan
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Bingkai Luar
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor("#0F766E") // Deep Emerald
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(
            RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN),
            16f, 16f,
            outerBorderPaint
        )

        // Bingkai Dalam Aksen Emas
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#D97706") // Amber Gold
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(MARGIN + 8f, MARGIN + 8f, PAGE_WIDTH - MARGIN - 8f, PAGE_HEIGHT - MARGIN - 8f),
            12f, 12f,
            innerBorderPaint
        )

        // 2. Header Sertifikat
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SERTIFIKAT PENGEMUDI RAMAH LINGKUNGAN", PAGE_WIDTH / 2f, 110f, titlePaint)

        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#0F766E")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
            isAntiAlias = true
        }
        canvas.drawText("MARI PARTNER FUEL — GREEN MOBILITY EXCELLENCE", PAGE_WIDTH / 2f, 138f, subHeaderPaint)

        val descPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Dengan bangga dan apresiasi tinggi, sertifikat ini dianugerahkan kepada:", PAGE_WIDTH / 2f, 190f, descPaint)

        // 3. Nama Pengemudi (Besar & Elegan)
        val namePaint = Paint().apply {
            color = Color.parseColor("#0284C7")
            textSize = 32f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(report.driverName.uppercase(), PAGE_WIDTH / 2f, 240f, namePaint)

        // Garis Pembatas Nama
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }
        canvas.drawLine((PAGE_WIDTH / 2f) - 180f, 255f, (PAGE_WIDTH / 2f) + 180f, 255f, dividerPaint)

        // 4. Penghargaan & Kendaraan
        val statementPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "Atas konsistensi berkendara efisien dan komitmen meminimalisir emisi karbon bersama armada:",
            PAGE_WIDTH / 2f,
            280f,
            statementPaint
        )

        val vehiclePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("${report.vehicleName} (${report.vehicleType})", PAGE_WIDTH / 2f, 305f, vehiclePaint)

        // 5. Kartu Badge Prestasi & Metrik Hijau
        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val badgeRect = RectF(120f, 335f, PAGE_WIDTH - 120f, 445f)
        canvas.drawRoundRect(badgeRect, 16f, 16f, cardBgPaint)
        canvas.drawRoundRect(badgeRect, 16f, 16f, cardBorderPaint)

        // Kolom 1: Skor Eco-Driver
        drawMetricCol(
            canvas = canvas,
            centerX = 220f,
            centerY = 390f,
            value = "${report.ecoScore} / 100",
            label = "SKOR ECO-DRIVER",
            accentColor = Color.parseColor("#0F766E")
        )

        // Kolom 2: Gelar Predikat
        drawMetricCol(
            canvas = canvas,
            centerX = 421f,
            centerY = 390f,
            value = report.badgeTitle,
            label = "PREDIKAT EFISIENSI",
            accentColor = Color.parseColor("#D97706")
        )

        // Kolom 3: CO2 Terhemat
        drawMetricCol(
            canvas = canvas,
            centerX = 622f,
            centerY = 390f,
            value = "${report.co2SavedKg} kg",
            label = "EMISI CO₂ TERHEMAT",
            accentColor = Color.parseColor("#16A34A")
        )

        // 6. Footer, Tanda Tangan Digital & ID Sertifikat
        val footerLabelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val footerValuePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // Tanggal Terbit
        canvas.drawText("Tanggal Penerbitan Resmi:", 120f, 490f, footerLabelPaint)
        canvas.drawText(report.issueDate, 120f, 508f, footerValuePaint)

        // Nomor Lisensi / ID Sertifikat
        canvas.drawText("Nomor Identifikasi Digital:", 120f, 530f, footerLabelPaint)
        canvas.drawText(report.certificateId, 120f, 546f, footerValuePaint)

        // Tanda Tangan Eksekutif Digital
        val signAlignRight = Paint(footerLabelPaint).apply { textAlign = Paint.Align.RIGHT }
        val signValueRight = Paint(footerValuePaint).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawText("Diverifikasi secara Digital oleh:", PAGE_WIDTH - 120f, 490f, signAlignRight)
        canvas.drawText("Mari Partner Fuel Verification Authority", PAGE_WIDTH - 120f, 508f, signValueRight)
        canvas.drawText("Algoritma Standar Emisi Karbon Terverifikasi", PAGE_WIDTH - 120f, 530f, signAlignRight)
    }

    private fun drawMetricCol(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        value: String,
        label: String,
        accentColor: Int
    ) {
        val valPaint = Paint().apply {
            color = accentColor
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val lblPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
            isAntiAlias = true
        }

        canvas.drawText(value, centerX, centerY - 4f, valPaint)
        canvas.drawText(label, centerX, centerY + 22f, lblPaint)
    }
}
