package id.maripartner.fuel.core.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class ThousandSeparatorVisualTransformationTest {

    private val transformation = ThousandSeparatorVisualTransformation()

    @Test
    fun filter_emptyInput_returnsEmpty() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
    }

    @Test
    fun filter_lessThanThousand_returnsUnchanged() {
        val result = transformation.filter(AnnotatedString("250"))
        assertEquals("250", result.text.text)
    }

    @Test
    fun filter_thousandAmount_addsDot() {
        val result = transformation.filter(AnnotatedString("25000"))
        assertEquals("25.000", result.text.text)
    }

    @Test
    fun filter_hundredThousand_addsDot() {
        val result = transformation.filter(AnnotatedString("250000"))
        assertEquals("250.000", result.text.text)
    }

    @Test
    fun filter_millionAmount_addsMultipleDots() {
        val result = transformation.filter(AnnotatedString("1500000"))
        assertEquals("1.500.000", result.text.text)
    }

    @Test
    fun filter_withPrefix_formatsCorrectly() {
        val prefixed = ThousandSeparatorVisualTransformation(prefix = "Rp ")
        val result = prefixed.filter(AnnotatedString("25000"))
        assertEquals("Rp 25.000", result.text.text)
    }

    @Test
    fun offsetMapping_correctlyMapsPositions() {
        val result = transformation.filter(AnnotatedString("25000"))
        val mapping = result.offsetMapping

        // Original "25000" -> Transformed "25.000"
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(1, mapping.originalToTransformed(1))
        assertEquals(3, mapping.originalToTransformed(2)) // after '5' and dot -> 3
        assertEquals(4, mapping.originalToTransformed(3))
        assertEquals(5, mapping.originalToTransformed(4))
        assertEquals(6, mapping.originalToTransformed(5))

        // Transformed back to original
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(1, mapping.transformedToOriginal(1))
        assertEquals(2, mapping.transformedToOriginal(2)) // dot
        assertEquals(2, mapping.transformedToOriginal(3)) // first '0'
        assertEquals(3, mapping.transformedToOriginal(4)) // second '0'
        assertEquals(4, mapping.transformedToOriginal(5)) // third '0'
        assertEquals(5, mapping.transformedToOriginal(6)) // end of text
    }
}
