package com.lenta.bp7.data.model

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.function.Executable

internal class GoodTest {

    private var good: Good? = null

    private val id = 0
    private val ean = "4605996001633"
    private val material = "000000000000000021"
    private val matcode = "370105000021"
    private val enteredCode = EnteredCode.EAN
    private val name = "Р/к горбуша (Россия) 230/250г"
    private val unitsCode = "ST"
    private val units = "шт"

    @BeforeEach
    fun createGood() {
        good = Good(
                id = id,
                ean = ean,
                material = material,
                matcode = matcode,
                enteredCode = enteredCode,
                name = name,
                unitsCode = unitsCode,
                units = units)
    }

    @AfterEach
    fun deleteGood() {
        good = null
    }

    @Test
    fun `Good creation`() {
        assertAll("good",
                Executable { assertEquals(id, good?.id) },
                Executable { assertEquals(ean, good?.ean) },
                Executable { assertEquals(material, good?.material) },
                Executable { assertEquals(matcode, good?.matcode) },
                Executable { assertEquals(enteredCode, good?.enteredCode) },
                Executable { assertEquals(name, good?.name) },
                Executable { assertEquals(unitsCode, good?.unitsCode) },
                Executable { assertEquals(units, good?.units) },
                Executable { assertEquals(0, good?.facings) },
                Executable { assertEquals(GoodStatus.CREATED, good?.getStatus()) }
        )
    }

    @Test
    fun `Change status`() {
        good?.setStatus(GoodStatus.PROCESSED)
        assertEquals(GoodStatus.PROCESSED, good?.getStatus())
    }

    @Test
    fun `Change facings`() {
        good?.facings = 11
        assertEquals(11, good?.facings)
    }

    @Test
    fun `Get last six digit of sap-code`() {
        assertEquals(6, good?.getFormattedMaterial()?.length)
    }

    @Test
    fun `Empty facings with CREATED status`() {
        good?.facings = 0
        good?.setStatus(GoodStatus.CREATED)
        assertEquals("+", good?.getFacingOrPlus())
    }

    @Test
    fun `Empty facings with PROCESSED status`() {
        good?.facings = 0
        good?.setStatus(GoodStatus.PROCESSED)
        assertEquals("+", good?.getFacingOrPlus())
    }

    @Test
    fun `Not empty facings`() {
        good?.facings = 12
        assertEquals("12", good?.getFacingOrPlus())
    }

}