package com.lenta.bp7.data.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.text.SimpleDateFormat
import java.util.*

internal class SegmentTest {

    private lateinit var segment: Segment

    private val id = 0
    private val storeNumber = "0001"
    private val number = "111-111"

    @BeforeEach
    fun createSegment() {
        segment = Segment(
                id = id,
                storeNumber = storeNumber,
                number = number)
    }

    @Test
    fun `Segment creation`() {
        assertAll("segment",
                Executable { assertEquals(id, segment.id) },
                Executable { assertEquals(storeNumber, segment.storeNumber) },
                Executable { assertEquals(number, segment.number) },
                Executable { assertTrue(segment.checkFinish == null) },
                Executable { assertEquals(SegmentStatus.UNFINISHED, segment.getStatus()) },
                Executable { assertTrue(segment.shelves.isEmpty()) }
        )
    }

    @Test
    fun `Change status`() {
        segment.setStatus(SegmentStatus.PROCESSED)
        assertEquals(SegmentStatus.PROCESSED, segment.getStatus())
        assertTrue(segment.checkFinish != null )
    }

    @Test
    fun `Get formatted start date`() {
        val formattedStartDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(segment.checkStart)
        assertEquals(formattedStartDate, segment.getFormattedDate())
    }

}