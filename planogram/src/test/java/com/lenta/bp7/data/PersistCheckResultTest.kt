package com.lenta.bp7.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mobrun.plugin.api.HyperHive
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class PersistCheckResultTest  {

    private lateinit var persistCheckResult: IPersistCheckResult

    @BeforeEach
    fun createPersistCheckResult() {
        val hyperHive: HyperHive = mock()
        val gson: Gson = GsonBuilder().create()
        persistCheckResult = PersistCheckResult(hyperHive, gson)
    }

    @Test
    fun saveCheckResult() {
        assertEquals(4, 2 + 2)
    }
}