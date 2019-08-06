package com.lenta.bp14

import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.CoreApplication
import com.lenta.shared.settings.DefaultConnectionSettings

class AndroidApplication : CoreApplication() {
    override fun getDefaultConnectionSettings(): DefaultConnectionSettings {
        return DefaultConnectionSettings(
                serverAddress = "http://9.6.24.46",
                environment = "Lenta_LRQ",
                project = "PR_SHARED",
                testServerAddress = "http://9.6.24.46",
                testEnvironment = "Lenta_LRQ",
                testProject = "PR_SHARED",
                techLogin = "MAKAROV",
                techPassword = "1q2w3e4r"
        )
    }

    override fun onHandleException() {
        ExceptionHandler().let {
            getAppComponent(coreComponent).inject(it)
            it.handleException()
        }

    }
}

class ExceptionHandler {

    fun handleException() {
        //TODO need implement
    }
}