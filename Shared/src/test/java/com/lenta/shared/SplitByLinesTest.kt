package com.lenta.shared

import com.lenta.shared.utilities.extentions.splitByLines
import junit.framework.TestCase.assertEquals
import org.junit.Test


class SplitByLinesTest {
    @Test
    fun addition_isCorrect() {
        "split by lines".splitByLines(oneLineMaxLength = 10).apply {
            assertEquals(2, this.size)
            assertEquals("split by", this[0])
            assertEquals("lines", this[1])
        }

        "Denotes that the annotated method".splitByLines(oneLineMaxLength = 16).apply {
            assertEquals(2, this.size)
            assertEquals("Denotes that the", this[0])
            assertEquals("annotated method", this[1])
        }

    }
}