package com.lenta.shared

import android.content.Context
import com.lenta.shared.platform.app_update.AppUpdateChecker
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppUpdateCheckerTest {
    @MockK
    lateinit var context: Context

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun `определение необходимости обновления приложения`() {

        val appUpdateChecker = AppUpdateChecker(context)

        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.164.29bd1"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.164.29bd1"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "9.99.999.29bd1"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "9.999.999.29bd1"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.7.0.1", "10.0.165.29bd1"))

        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.165.29bd1"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.166.29bd1"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "11.0.0.29bd1"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.1.0.29bd1"))


        mockkObject(appUpdateChecker)
        every {appUpdateChecker.getCurrentVersionAppName()} returns "10.0.164.29bd1"

        assertTrue(appUpdateChecker.isNeedUpdate("10.7.0.1"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165"))

        assertFalse(appUpdateChecker.isNeedUpdate("10.0.164"))

    }


}
