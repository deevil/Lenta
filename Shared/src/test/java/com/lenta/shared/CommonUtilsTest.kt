package com.lenta.shared

import android.util.Base64
import com.lenta.shared.utilities.getBaseAuth
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.nio.charset.Charset


@RunWith(MockitoJUnitRunner::class)
class CommonUtilsTest {

    @Before
    fun beforeTest() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) }.coAnswers {
            println("text: ${(args[0] as ByteArray).toString(Charset.defaultCharset())}")
            java.util.Base64.getEncoder().encodeToString(args[0] as ByteArray).apply {
                println("encodeToString: $this")
            } + "\n"
        }
    }


    @Test
    fun baseAuthEncode() {
        var login = "makak"
        var password = "testtest"
        assertEquals("Basic bWFrYWs6dGVzdHRlc3Q=", getBaseAuth(login, password))

        login = "_dsdfdfdsf"
        password = "_dsdfdfdsf"
        assertEquals("Basic X2RzZGZkZmRzZjpfZHNkZmRmZHNm", getBaseAuth(login, password))

        login = "/*-789645asdsdfgfdghgjhkjjk;_)(*&&!@#\$%^`"
        password = "/*-789645asdsdfgfdghgjhkjjk;_)(*&&!@#\$%^`"
        assertEquals(
                "Basic LyotNzg5NjQ1YXNkc2RmZ2ZkZ2hnamhramprO18pKComJiFAIyQlXmA6LyotNzg5NjQ1YXNkc2RmZ2ZkZ2hnamhramprO18pKComJiFAIyQlXmA=",
                getBaseAuth(login, password))
    }


    @Test
    fun testMockExample() {
        val mockT: Trojan = mock()
        // mock objects return a default value when not stubbed
        assertNull("horse", mockT.open())

        Mockito.`when`(mockT.open()).thenReturn("dog")

        assertEquals("dog", mockT.open())
    }

    @After
    fun afterTest() {
        unmockkAll()
    }


}

class Trojan {
    fun open(): String {
        return "horse"
    }
}