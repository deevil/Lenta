package com.lenta.shared

import android.content.Context
import com.lenta.shared.platform.app_update.AppUpdateChecker
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mock


@RunWith(MockitoJUnitRunner::class)
class AppUpdateCheckerTest {
    @Mock
    lateinit var context: Context


    @Test
    fun appUpdateCheckerTest() {
        val appUpdateChecker = AppUpdateChecker(context)
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.164"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "9.99.999"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.0.165", "9.999.999"))
        assertTrue(appUpdateChecker.isNeedUpdate("10.7.0.1", "10.0.165"))

        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.165"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.0.166"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "11.0.0"))
        assertFalse(appUpdateChecker.isNeedUpdate("10.0.165", "10.1.0"))
    }


}
