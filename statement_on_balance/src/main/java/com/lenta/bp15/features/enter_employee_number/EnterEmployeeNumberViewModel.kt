package com.lenta.bp15.features.enter_employee_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.features.select_personnel_number.SelectPersonnelNumberDelegate
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class EnterEmployeeNumberViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var selectPersonnelNumberDelegate: SelectPersonnelNumberDelegate

    @Inject
    lateinit var manager: ITaskManager


    val editTextFocus = MutableLiveData<Boolean>()

    private val nextButtonFocus = MutableLiveData<Boolean>()

    val personnelNumber = MutableLiveData("")

    val fullName = MutableLiveData("")

    val employeesPosition = MutableLiveData("")

    val enabledNextButton = fullName.map { !it.isNullOrBlank() }

    init {
        launchUITryCatch {
            selectPersonnelNumberDelegate.personnelNumber = personnelNumber
            selectPersonnelNumberDelegate.fullName = fullName
            selectPersonnelNumberDelegate.employeesPosition = employeesPosition
            selectPersonnelNumberDelegate.editTextFocus = editTextFocus
            selectPersonnelNumberDelegate.nextButtonFocus = nextButtonFocus

            selectPersonnelNumberDelegate.init(
                    viewModelScope = this@EnterEmployeeNumberViewModel::viewModelScope,
                    onNextScreenOpen = {
                        navigator.openMainMenuScreen()
                    }
            )
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return selectPersonnelNumberDelegate.onOkInSoftKeyboard()
    }

    fun onClickNext() {
        if (manager.isExistUnsavedData()) {
            selectPersonnelNumberDelegate.savePersonnelNumber()
            navigator.showUnsavedDataFoundOnDevice(
                    deleteCallback = {
                        manager.removeCurrentTaskBackup()
                        navigator.openMainMenuScreen()
                    },
                    goOverCallback = {
                        navigator.showProgressLoadingData()

                        manager.restoreCurrentTask()
                        navigator.openMainMenuScreen()
                        navigator.openTaskListScreen()
                        navigator.openTaskCardScreen()

                        navigator.hideProgress()
                    }
            )
        } else {
            selectPersonnelNumberDelegate.onClickNext()
        }
    }

    fun onResume() {
        selectPersonnelNumberDelegate.onResume()
    }


    fun onScanResult(data: String) {
        selectPersonnelNumberDelegate.onScanResult(data)
    }

}
