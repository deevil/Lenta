package com.lenta.inventory.features.main_menu

import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.platform.navigation.IScreenNavigator
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

    fun onClickWorkWithTask() {
        screenNavigator.openLoadingTasksScreen()
    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen(true)

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}