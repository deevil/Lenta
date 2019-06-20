package com.lenta.bp10

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.shared.utilities.tests_utils.observeOnce
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    lateinit var authViewModel: AuthViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()


    @Before
    fun setUp() {

        Dispatchers.setMain(mainThreadSurrogate)

        authViewModel = AuthViewModel()
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
        mainThreadSurrogate.close()
    }


    @Test(expected = NoSuchMethodError::class)
    fun onClickAuxiliaryMenuTest() {

        Mockito.`when`(authViewModel.navigator.openAuxiliaryMenuScreen()).thenThrow(NoSuchMethodError())

        authViewModel.onClickAuxiliaryMenu()

    }

    @Test
    fun enterEnabledTest() {

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



