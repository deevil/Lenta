package com.lenta.bp10

import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.CoreApplication
import com.lenta.shared.settings.DefaultConnectionSettings
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class AndroidApplication : CoreApplication() {
    override fun getDefaultConnectionSettings(): DefaultConnectionSettings {
        Logg.d { "default serverAddress: ${getString(R.string.ds_serverAddress)}"}
        return DefaultConnectionSettings(
                serverAddress = getString(R.string.ds_serverAddress),
                environment = getString(R.string.ds_environment),
                project = getString(R.string.ds_project),
                testServerAddress = getString(R.string.ds_testServerAddress),
                testEnvironment = getString(R.string.ds_testEnvironment),
                testProject = getString(R.string.ds_testProject),
                techLogin = getString(R.string.ds_techLogin),
                techPassword = getString(R.string.ds_techPassword)
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