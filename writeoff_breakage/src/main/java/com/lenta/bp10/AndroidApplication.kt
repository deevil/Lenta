package com.lenta.bp10

import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.CoreApplication
import com.lenta.shared.settings.DefaultConnectionSettings
import javax.inject.Inject

class AndroidApplication : CoreApplication() {
    override fun getDefaultConnectionSettings(): DefaultConnectionSettings {
        return DefaultConnectionSettings(
                serverAddress = "http://9.6.24.110",
                environment = "Lenta_LRQ",
                project = "PR_WOB",
                testServerAddress = "http://9.6.24.110",
                testEnvironment = "Lenta_LRQ",
                testProject = "PR_WOB",
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
    @Inject
    lateinit var writeOffTaskManager: IWriteOffTaskManager
    @Inject
    lateinit var persistWriteOffTask: IPersistWriteOffTask

    fun handleException() {
        persistWriteOffTask.saveWriteOffTask(writeOffTaskManager.getWriteOffTask())
    }
}