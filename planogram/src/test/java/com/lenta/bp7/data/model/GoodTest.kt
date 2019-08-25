package com.lenta.bp7.data.model

import com.lenta.shared.models.core.Uom
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.function.Executable

internal class GoodTest {

    private lateinit var good: Good

    private val id = 0
    private val ean = "4605996001633"
    private val material = "000000000000000021"
    private val matcode = "370105000021"
    private val enteredCode = EnteredCode.EAN
    private val name = "Р/к горбуша (Россия) 230/250г"
    private val uom = Uom.DEFAULT

    private val customGoodEan = "XXXXXXXX"

    @BeforeEach
    fun createGood() {
        good = Good(
                id = id,
                ean = ean,
                material = material,
                matcode = matcode,
                enteredCode = enteredCode,
                name = name,
                uom = uom)
    }

    private fun createCustomGood(
            id: Int = 0,
            ean: String = "" + (10000000..99999999999999).random(),
            material: String = "000000000000" + (100000..999999).random(),
            matcode: String = "" + (100000000000..999999999999).random(),
            enteredCode: EnteredCode = EnteredCode.EAN,
            name: String = "Good " + (1..999).random(),
            facings: Int = (1..15).random(),
            uom: Uom = Uom.DEFAULT,
            status: GoodStatus = GoodStatus.CREATED
    ): Good {
        return Good(id, ean, material, matcode, enteredCode, name, facings, uom, status)
    }

    @Test
    fun `Good creation`() {
        assertAll("good",
                Executable { assertEquals(id, good.id) },
                Executable { assertEquals(ean, good.ean) },
                Executable { assertEquals(material, good.material) },
                Executable { assertEquals(matcode, good.matcode) },
                Executable { assertEquals(enteredCode, good.enteredCode) },
                Executable { assertEquals(name, good.name) },
                Executable { assertEquals(uom, good.uom) },
                Executable { assertEquals(0, good.facings) },
                Executable { assertEquals(GoodStatus.CREATED, good.getStatus()) }
        )
    }

    @Test
    fun `Change status`() {
        good.setStatus(GoodStatus.PROCESSED)
        assertEquals(GoodStatus.PROCESSED, good.getStatus())
    }

    @Test
    fun `Change facings`() {
        good.facings = 11
        assertEquals(11, good.facings)
    }

    @Test
    fun `Get last six digit of sap-code`() {
        assertEquals(6, good.getFormattedMaterial()?.length)
    }

    @Test
    fun `Get facings from facingsOrPlus`() {
        good.facings = 12
        assertEquals("12", good.getFacingOrPlus())
    }

    @Test
    fun `Get plus from facingsOrPlus with CREATED status`() {
        good.facings = 0
        good.setStatus(GoodStatus.CREATED)
        assertEquals("+", good.getFacingOrPlus())
    }

    @Test
    fun `Get plus from facingsOrPlus with PROCESSED status`() {
        good.facings = 0
        good.setStatus(GoodStatus.PROCESSED)
        assertEquals("+", good.getFacingOrPlus())
    }

    @Test
    fun `Get ean from getEanOrEmpty`() {
        good = createCustomGood(ean = customGoodEan)
        assertEquals(customGoodEan, good.getEanOrEmpty())
    }

    @Test
    fun `Get empty from getEanOrEmpty`() {
        good = createCustomGood(ean = "")
        assertEquals("", good.getEanOrEmpty())
    }

}