package com.lenta.bp7.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lenta.bp7.data.model.*
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.HyperHiveState
import com.mobrun.plugin.api.StateAPI
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito


internal class PersistCheckResultTest  {

    private val keyForSave = "PLANOGRAM_CHECK_RESULT"
    private var temp = ""

    private lateinit var persistCheckResult: IPersistCheckResult
    private lateinit var checkData: CheckData


    @BeforeEach
    fun createPersistCheckResult() {
        /*val hyperHiveState: HyperHiveState = mock()
        val gson: Gson = GsonBuilder().create()
        val hyperHive: HyperHive = mock()

        persistCheckResult = PersistCheckResult(hyperHive, gson)*/

        checkData = CheckData(mock())
        checkData.let {
            it.marketNumber = "0001"
            it.checkType = CheckType.SELF_CONTROL
        }
    }

    private fun addSegment(
            number: String = "" + (100..999).random() + "-" + (100..999).random(),
            status: SegmentStatus = SegmentStatus.UNFINISHED) {
        checkData.addSegment("0001", number)
        checkData.getCurrentSegment()?.setStatus(status)
    }

    private fun addShelf(
            number: String = "" + (1..999).random(),
            status: ShelfStatus = ShelfStatus.UNFINISHED) {
        if (checkData.getCurrentSegment() == null) addSegment()
        checkData.addShelf(number)
        checkData.getCurrentShelf()?.setStatus(status)
    }

    private fun addGood(
            ean: String = "" + (10000000..99999999999999).random(),
            material: String = "000000000000" + (100000..999999).random(),
            matcode: String = "" + (100000000000..999999999999).random(),
            enteredCode: EnteredCode = EnteredCode.EAN,
            name: String = "Good " + (1..999).random(),
            facings: Int = (1..99).random(),
            uom: Uom = Uom.DEFAULT,
            status: GoodStatus = GoodStatus.CREATED) {
        if (checkData.getCurrentSegment() == null) addSegment()
        if (checkData.getCurrentShelf() == null) addShelf()
        checkData.addGood(GoodInfo(ean, material, matcode, enteredCode, name, uom))
        checkData.getCurrentGood()?.facings = facings
        checkData.getCurrentGood()?.setStatus(status)
    }

    @Test
    fun `Save check result with empty checkData`() {
        val gson: Gson = GsonBuilder().create()
        val hyperHive: HyperHive = mock()
        persistCheckResult = PersistCheckResult(hyperHive, gson)

        //doNothing().`when`(hyperHive).stateAPI.saveParamToDB(keyForSave, null)
        val stateAPI: StateAPI = mock()
        whenever(hyperHive.stateAPI).thenReturn(stateAPI)
        doNothing().whenever(stateAPI.saveParamToDB(keyForSave, null))

        persistCheckResult.saveCheckResult(checkData)

        verify(hyperHive).stateAPI.saveParamToDB(keyForSave, null)
    }

    @Test
    fun `Save check result with checkData`() {
        val gson: Gson = GsonBuilder().create()
        val hyperHive: HyperHive = mock()
        persistCheckResult = PersistCheckResult(hyperHive, gson)

        addGood()
        persistCheckResult.saveCheckResult(checkData)

        verify(hyperHive).stateAPI.saveParamToDB(keyForSave, null)
    }

    @Test
    fun `Get saved check result`() {
        val gson: Gson = GsonBuilder().create()
        val hyperHive: HyperHive = mock()
        persistCheckResult = PersistCheckResult(hyperHive, gson)


        whenever(hyperHive.stateAPI.getParamFromDB(keyForSave)).thenReturn("something...")

        persistCheckResult.getSavedCheckResult()

        verify(hyperHive).stateAPI.getParamFromDB(keyForSave)
    }
}