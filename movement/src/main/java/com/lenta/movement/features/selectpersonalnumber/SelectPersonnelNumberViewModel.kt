package com.lenta.movement.features.selectpersonalnumber

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.select_personnel_number.SelectPersonnelNumberDelegate
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectPersonnelNumberViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var selectPersonnelNumberDelegate: SelectPersonnelNumberDelegate

    @Inject
    lateinit var appSettings: IAppSettings

    val personnelNumber = MutableLiveData("")
    val fullName = MutableLiveData("")
    val employeesPosition = MutableLiveData("")

    val editTextFocus = MutableLiveData<Boolean>()
    val nextButtonFocus = MutableLiveData<Boolean>()
    val nextButtonEnabled = fullName.map { !it.isNullOrBlank() }

    private var codeConfirm: Int? = null

    fun setCodeConfirm(codeConfirm: Int?) {
        this.codeConfirm = codeConfirm
    }

    init {
        viewModelScope.launch {
            val lastPersonnelNumber = appSettings.lastPersonnelNumber
            lastPersonnelNumber?.let {
                personnelNumber.value = lastPersonnelNumber
            }
            selectPersonnelNumberDelegate.personnelNumber = personnelNumber
            selectPersonnelNumberDelegate.fullName = fullName
            selectPersonnelNumberDelegate.employeesPosition = employeesPosition
            selectPersonnelNumberDelegate.editTextFocus = editTextFocus
            selectPersonnelNumberDelegate.nextButtonFocus = nextButtonFocus
            selectPersonnelNumberDelegate.init(
                    viewModelScope = this@SelectPersonnelNumberViewModel::viewModelScope,
                    onNextScreenOpen = {
                        screenNavigator.openMainMenuScreen()
                    }
            )
            selectPersonnelNumberDelegate.codeConfirm = codeConfirm
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        selectPersonnelNumberDelegate.handleFailure(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return selectPersonnelNumberDelegate.onOkInSoftKeyboard()
    }

    fun onClickNext() = selectPersonnelNumberDelegate.onClickNext()

    fun onResume() = selectPersonnelNumberDelegate.onResume()

    fun onScanResult(data: String) = selectPersonnelNumberDelegate.onScanResult(data)
}
