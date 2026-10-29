package id.maripartner.fuel.data.remote.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReceiptStorageManagerTest {

    private lateinit var storageManager: ReceiptStorageManager

    @Before
    fun setup() {
        storageManager = ReceiptStorageManager()
    }

    @Test
    fun generateStoragePath_createsUserScopedPathWithExtension() {
        val userId = "user-uuid-1234"
        val jpegPath = storageManager.generateStoragePath(userId, "image/jpeg")
        assertTrue(jpegPath.startsWith("user-uuid-1234/"))
        assertTrue(jpegPath.endsWith(".jpg"))

        val pngPath = storageManager.generateStoragePath(userId, "image/png")
        assertTrue(pngPath.startsWith("user-uuid-1234/"))
        assertTrue(pngPath.endsWith(".png"))

        val webpPath = storageManager.generateStoragePath(userId, "image/webp")
        assertTrue(webpPath.startsWith("user-uuid-1234/"))
        assertTrue(webpPath.endsWith(".webp"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun generateStoragePath_throwsOnBlankUserId() {
        storageManager.generateStoragePath("", "image/jpeg")
    }

    @Test
    fun validateReceiptFile_acceptsAllowedSizeAndMime() {
        val validSize = 2 * 1024 * 1024L // 2 MB
        assertTrue(storageManager.validateReceiptFile(validSize, "image/jpeg").isSuccess)
        assertTrue(storageManager.validateReceiptFile(validSize, "image/png").isSuccess)
        assertTrue(storageManager.validateReceiptFile(validSize, "image/webp").isSuccess)
    }

    @Test
    fun validateReceiptFile_rejectsZeroOrNegativeSize() {
        assertFalse(storageManager.validateReceiptFile(0L, "image/jpeg").isSuccess)
        assertFalse(storageManager.validateReceiptFile(-100L, "image/jpeg").isSuccess)
    }

    @Test
    fun validateReceiptFile_rejectsExceedingMaxSize() {
        val oversized = 5 * 1024 * 1024L + 1 // 5 MB + 1 byte
        val result = storageManager.validateReceiptFile(oversized, "image/jpeg")
        assertFalse(result.isSuccess)
    }

    @Test
    fun validateReceiptFile_rejectsDisallowedMimeTypes() {
        assertFalse(storageManager.validateReceiptFile(1024L, "application/pdf").isSuccess)
        assertFalse(storageManager.validateReceiptFile(1024L, "text/plain").isSuccess)
        assertFalse(storageManager.validateReceiptFile(1024L, "application/octet-stream").isSuccess)
    }
}
