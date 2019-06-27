package com.lenta.bp10.product_screen

import com.lenta.bp10.features.good_information.LimitsChecker
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LimitsCheckerTest {

    lateinit var limitsChecker: LimitsChecker


    @Test
    fun `Лимиты на алкоголь`() {

        var observeFuncWasExecuted = false

        limitsChecker = LimitsChecker(limit = 10.0, observer = { observeFuncWasExecuted = true })
        assertFalse(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)

        limitsChecker.check(9.0)
        assertFalse(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)

        limitsChecker.check(9.0)
        assertFalse(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)

        limitsChecker.check(11.0)
        assertTrue(limitsChecker.wasExceeded())
        assertTrue(observeFuncWasExecuted)

        observeFuncWasExecuted = false

        limitsChecker.check(28.0)
        assertTrue(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)

    }

    @Test
    fun `Нулевой лимит`() {

        var observeFuncWasExecuted = false

        limitsChecker = LimitsChecker(limit = 0.0, observer = { observeFuncWasExecuted = true })
        assertFalse(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)

        limitsChecker.check(9.0)
        assertFalse(limitsChecker.wasExceeded())
        assertFalse(observeFuncWasExecuted)



    }


}



