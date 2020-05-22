package com.lenta.bp16.features.select_personnel_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
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
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var selectPersonnelNumberDelegate: SelectPersonnelNumberDelegate

    val editTextFocus = MutableLiveData<Boolean>()
    private val nextButtonFocus = MutableLiveData<Boolean>()
    val isScreenMainMenu: MutableLiveData<Boolean> = MutableLiveData()
    val personnelNumber = MutableLiveData("")
    val fullName = MutableLiveData("")
    val employeesPosition = MutableLiveData("")
    val enabledBackButton by lazy {
        isScreenMainMenu
    }
    val enabledNextButton = fullName.map { !it.isNullOrBlank() }

    init {
        viewModelScope.launch {
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
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return selectPersonnelNumberDelegate.onOkInSoftKeyboard()
    }

    fun onClickNext() {
        selectPersonnelNumberDelegate.onClickNext()
    }


    fun onResume() {
        selectPersonnelNumberDelegate.onResume()
    }


    fun onScanResult(data: String) {
        selectPersonnelNumberDelegate.onScanResult(data)
    }

    fun onBackPressed() {
        screenNavigator.openMainMenuScreen()
    }

}
