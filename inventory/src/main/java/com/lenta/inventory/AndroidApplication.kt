package com.lenta.inventory

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.CoreApplication
import com.lenta.shared.settings.DefaultConnectionSettings

class AndroidApplication : CoreApplication() {
    override fun getDefaultConnectionSettings(): DefaultConnectionSettings {
        return DefaultConnectionSettings(
                serverAddress = "http://9.6.24.110",
                environment = "Lenta_LRQ",
                project = "PR_INV",
                testServerAddress = "http://9.6.24.110",
                testEnvironment = "Lenta_LRQ",
                testProject = "PR_INV",
                techLogin = "tech_user",
                techPassword = "123456"
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