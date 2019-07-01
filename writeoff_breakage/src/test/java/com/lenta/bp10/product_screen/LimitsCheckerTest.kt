package com.lenta.bp10.product_screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.features.good_information.LimitsChecker
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@Suppress("NonAsciiCharacters")
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LimitsCheckerTest {

    lateinit var limitsChecker: LimitsChecker

    @get:Rule
    val rule = InstantTaskExecutorRule()


    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }


    @Test
    fun `Лимиты на алкоголь`() = runBlocking {

        var observeFuncWasExecuted = false

        val countLiveData = MutableLiveData(0.0)

        limitsChecker = LimitsChecker(
                limit = 10.0,
                observer = { observeFuncWasExecuted = true },
                countLiveData = countLiveData,
                viewModelScope = ::getScope)
        assertFalse(observeFuncWasExecuted)

        countLiveData.value = 9.0
        limitsChecker.check()

        delay(300)

        assertFalse(observeFuncWasExecuted)


        countLiveData.value = 9.0
        limitsChecker.check()
        delay(300)
        assertFalse(observeFuncWasExecuted)

        countLiveData.value = 11.0
        limitsChecker.check()
        assertFalse(observeFuncWasExecuted)
        delay(300)
        assertTrue(observeFuncWasExecuted)

        observeFuncWasExecuted = false

        countLiveData.value = 28.0
        limitsChecker.check()
        assertFalse(observeFuncWasExecuted)
        delay(300)
        assertTrue(observeFuncWasExecuted)

    }

    private fun getScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }

    @Test
    fun `Нулевой лимит`() = runBlocking {

        var observeFuncWasExecuted = false

        val countLiveData = MutableLiveData(0.0)

        limitsChecker = LimitsChecker(limit = 0.0, observer = { observeFuncWasExecuted = true }, countLiveData = countLiveData, viewModelScope = ::getScope)
        assertFalse(observeFuncWasExecuted)

        countLiveData.value = 9.0
        limitsChecker.check()
        delay(500)
        assertFalse(observeFuncWasExecuted)


    }


}



