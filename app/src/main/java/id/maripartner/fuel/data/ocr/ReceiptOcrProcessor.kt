package id.maripartner.fuel.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import id.maripartner.fuel.core.common.result.AppException
import id.maripartner.fuel.core.common.result.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Pemroses OCR lokal menggunakan Google ML Kit Text Recognition on-device.
 */
class ReceiptOcrProcessor(
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    companion object {
        private const val MAX_BITMAP_DIMENSION = 1920
    }

    /**
     * Memproses citra bitmap struk menjadi teks mentah dengan pengoptimalan memori dan ukuran.
     */
    suspend fun processImage(bitmap: Bitmap): AppResult<String> = withContext(dispatcher) {
        try {
            val scaledBitmap = scaleBitmapIfNeeded(bitmap)
            val inputImage = InputImage.fromBitmap(scaledBitmap, 0)

            val rawText = suspendCancellableCoroutine<String> { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            if (rawText.isBlank()) {
                AppResult.Error(AppException.Validation("Tidak ada teks yang dapat dikenali pada gambar. Pastikan gambar jelas dan tidak terlalu gelap."))
            } else {
                AppResult.Success(rawText)
            }
        } catch (e: Exception) {
            AppResult.Error(AppException.Unknown("Gagal memproses OCR lokal: ${e.message}", e))
        }
    }

    /**
     * Menyesuaikan ukuran bitmap jika melebihi batas maksimal agar tidak membebani memori JVM/Android.
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_BITMAP_DIMENSION && height <= MAX_BITMAP_DIMENSION) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = MAX_BITMAP_DIMENSION
            targetHeight = (MAX_BITMAP_DIMENSION / ratio).toInt()
        } else {
            targetHeight = MAX_BITMAP_DIMENSION
            targetWidth = (MAX_BITMAP_DIMENSION * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Melepaskan resource Google ML Kit TextRecognizer.
     */
    fun close() {
        try {
            recognizer.close()
        } catch (_: Exception) { }
    }
}
