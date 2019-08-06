package com.lenta.bp7.data.model

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.function.Executable
import java.text.SimpleDateFormat
import java.util.*

internal class ShelfTest {

    private var shelf: Shelf? = null

    private val id = 0
    private val number = "11"

    @BeforeEach
    fun createShelf() {
        shelf = Shelf(
                id = id,
                number = number)
    }

    @AfterEach
    fun deleteShelf() {
        shelf = null
    }

    @Test
    fun `Shelf creation`() {
        assertAll("shelf",
                Executable { assertEquals(id, shelf?.id) },
                Executable { assertEquals(number, shelf?.number) },
                Executable { assertTrue(shelf?.checkStart != null ) },
                Executable { assertEquals(null, shelf?.checkFinish) },
                Executable { assertEquals(ShelfStatus.UNFINISHED, shelf?.getStatus()) },
                Executable { assertTrue(shelf?.goods?.isEmpty() == true) }
        )
    }

    @Test
    fun `Change status`() {
        shelf?.setStatus(ShelfStatus.PROCESSED)
        assertEquals(ShelfStatus.PROCESSED, shelf?.getStatus())
        assertTrue(shelf?.checkFinish != null )
    }

    @Test
    fun `Get formatted start time`() {
        val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(shelf?.checkStart)
        assertEquals(formattedStartTime, shelf?.getFormattedTime())
    }

}