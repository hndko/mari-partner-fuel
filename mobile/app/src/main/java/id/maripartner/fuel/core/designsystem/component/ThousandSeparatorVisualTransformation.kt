package id.maripartner.fuel.core.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation untuk memformat input angka/nominal dengan pemisah ribuan titik (.) khas Indonesia.
 * Contoh: "25000" ditampilkan secara otomatis menjadi "25.000".
 * Nilai state string yang disimpan tetap digit murni "25000" sehingga saat disimpan ke basis data
 * otomatis dinormalisasi menjadi angka murni tanpa perlu parsing manual.
 */
class ThousandSeparatorVisualTransformation(
    private val prefix: String = ""
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(
                text = if (prefix.isNotEmpty()) AnnotatedString(prefix) else AnnotatedString(""),
                offsetMapping = OffsetMapping.Identity
            )
        }

        val formattedBuilder = StringBuilder(prefix)
        val origToTrans = IntArray(raw.length + 1)
        val transToOrig = ArrayList<Int>(raw.length + (raw.length / 3) + prefix.length + 2)

        // Mapping untuk karakter prefix (jika ada) ke posisi 0 teks asli
        for (p in prefix.indices) {
            transToOrig.add(0)
        }

        var currentTransIdx = prefix.length

        for (i in raw.indices) {
            if (i > 0 && (raw.length - i) % 3 == 0) {
                formattedBuilder.append('.')
                transToOrig.add(i)
                currentTransIdx++
            }
            origToTrans[i] = currentTransIdx
            formattedBuilder.append(raw[i])
            transToOrig.add(i)
            currentTransIdx++
        }

        origToTrans[raw.length] = currentTransIdx
        transToOrig.add(raw.length)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return origToTrans[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transToOrig.size - 1)
                return transToOrig[clamped]
            }
        }

        return TransformedText(AnnotatedString(formattedBuilder.toString()), offsetMapping)
    }
}
