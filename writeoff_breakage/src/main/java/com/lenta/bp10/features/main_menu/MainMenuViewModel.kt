package com.lenta.bp10.features.main_menu

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val fio = MutableLiveData("")

    init {
        launchUITryCatch {
            fio.value = sessionInfo.personnelFullName
        }
    }

    fun onClickCreateTask() {
        screenNavigator.openLoadingTaskSettingsScreen()

    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen(codeConfirmation = null, isScreenMainMenu = true)

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}