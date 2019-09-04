package com.lenta.bp7.data.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.text.SimpleDateFormat
import java.util.*

internal class ShelfTest {

    private lateinit var shelf: Shelf
    private var creationDate = Date()

    private val id = 0
    private val number = "11"

    @BeforeEach
    fun createShelf() {
        shelf = Shelf(
                checkStart = creationDate,
                checkFinish = creationDate,
                id = id,
                number = number)
    }

    @Test
    fun `Shelf creation`() {
        assertAll("shelf",
                Executable { assertEquals(id, shelf.id) },
                Executable { assertEquals(number, shelf.number) },
                Executable { assertEquals(creationDate, shelf.checkFinish) },
                Executable { assertEquals(ShelfStatus.UNFINISHED, shelf.getStatus()) },
                Executable { assertTrue(shelf.goods.isEmpty()) }
        )
    }

    @Test
    fun `Change status`() {
        shelf.setStatus(ShelfStatus.PROCESSED)
        assertEquals(ShelfStatus.PROCESSED, shelf.getStatus())
        assertTrue(shelf.checkFinish != null)
    }

    @Test
    fun `Get formatted start time`() {
        val formattedStartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(shelf.checkStart)
        assertEquals(formattedStartTime, shelf.getFormattedTime())
    }

}