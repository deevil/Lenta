package com.lenta.bp10

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.shared.functional.Either
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.utilities.tests_utils.observeOnce
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.TestCase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {


    lateinit var authViewModel: AuthViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()


    @Before
    fun setUp() {

        Dispatchers.setMain(TestCoroutineDispatcher())

        authViewModel = AuthViewModel()
        authViewModel.auth = mock()
        authViewModel.appSettings = mock()
        authViewModel.sessionInfo = mock()
        authViewModel.permissionsRequest = mock()
        authViewModel.navigator = mock()


        println(authViewModel)
        println(authViewModel.appSettings)
        println(authViewModel.sessionInfo)
        println(authViewModel.permissionsRequest)
        println(authViewModel.navigator)


    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }


    @Test(expected = NoSuchMethodError::class)
    fun `Нажатие кнопки настройки`() {

        mockkObject(authViewModel.navigator)

        every { authViewModel.navigator.openAuxiliaryMenuScreen() } throws NoSuchMethodError()

        authViewModel.onClickAuxiliaryMenu()

    }

    @Test
    fun `Восстановление последнего логина`() {

        mockkObject(authViewModel.appSettings)
        every { authViewModel.appSettings.lastLogin } returns ("JACK LONDON")


        assertEquals("", authViewModel.login.value)
        authViewModel.onResume()
        assertEquals("JACK LONDON", authViewModel.login.value)


    }

    @Test
    fun `Обрезка пробелов логина и пароля`() = runBlocking {

        var handleFuncFlag = false

        whenever(authViewModel.auth.invoke(AuthParams("JACK LONDON", "123456"))).thenReturn(Either.Left({
            handleFuncFlag = true
            com.lenta.shared.exception.Failure.AuthError
        }()))


        authViewModel.login.value = " JACK LONDON "
        authViewModel.password.value = " 123456 "
        authViewModel.onClickEnter()

        assertTrue(handleFuncFlag)

        handleFuncFlag = false
        authViewModel.login.value = " Hemingway "
        authViewModel.password.value = " 123456 "
        authViewModel.onClickEnter()
        assertFalse(handleFuncFlag)

    }

    @Test
    fun `Активация кнопки входа`() {

        assertTrue(authViewModel.enterEnabled.value != true)

        authViewModel.login.value = ""
        authViewModel.password.value = "p"
        authViewModel.enterEnabled.observeOnce {
            assertFalse(it)
        }

        authViewModel.login.value = ""
        authViewModel.password.value = ""
        authViewModel.enterEnabled.observeOnce {
            assertFalse(it)
        }

        authViewModel.login.value = "l"
        authViewModel.enterEnabled.observeOnce {
            assertFalse(it)
        }

        authViewModel.password.value = "p"

        authViewModel.enterEnabled.observeOnce {
            assertTrue(it)
        }

        authViewModel.progress.value = true

        authViewModel.enterEnabled.observeOnce {
            assertFalse(it)
        }
    }


}



