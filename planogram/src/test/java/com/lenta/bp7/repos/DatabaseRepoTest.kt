package com.lenta.bp7.repos

import com.lenta.shared.fmp.resources.dao_ext.getSelfControlPinCode
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
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
    private lateinit var settings: ZmpUtz14V001

    @BeforeAll
    fun setUpp() {
        settings = mockk()
        databaseRepo = DatabaseRepo(hyperHive = mock(), settings = settings)
    }

    @Test
    fun `Getting self control pin code`() = runBlocking {
        mockkStatic("com.lenta.shared.fmp.resources.dao_ext.ZmpUtz14V001Kt").apply {
            every { settings.getSelfControlPinCode() } returns ("1111")
            assertEquals("1111", databaseRepo.getSelfControlPinCode())
        }
    }
}