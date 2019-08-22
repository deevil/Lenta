package com.lenta.bp7.repos

import com.lenta.shared.fmp.resources.dao_ext.getSelfControlPinCode
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DatabaseRepoTest {

    private lateinit var databaseRepo: IDatabaseRepo

    private lateinit var units: ZmpUtz07V001
    private lateinit var settings: ZmpUtz14V001
    private lateinit var stores: ZmpUtz23V001
    private lateinit var goodInfo: ZfmpUtz48V001
    private lateinit var barCodeInfo: ZmpUtz25V001

    @BeforeAll
    fun setUpp() {
        units = mockk()
        settings = mockk()
        stores = mockk()
        goodInfo = mockk()
        barCodeInfo = mockk()

        databaseRepo = DatabaseRepo(
                hyperHive = mock(),
                units = units,
                settings = settings,
                stores = stores,
                goodInfo = goodInfo,
                barCodeInfo = barCodeInfo)
    }

    @Test
    fun `Getting self control pin code`() = runBlocking {
        mockkStatic("com.lenta.shared.fmp.resources.dao_ext.ZmpUtz14V001Kt").apply {
            every { settings.getSelfControlPinCode() } returns ("1111")
            assertEquals("1111", databaseRepo.getSelfControlPinCode())
        }
    }
}