package com.lenta.inventory.features.main_menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val fio = MutableLiveData("")

    init {
        viewModelScope.launch {
            fio.value = sessionInfo.personnelFullName
        }
    }

    fun onClickWorkWithTask() {
        //TODO need to open next screen
        screenNavigator.openSetsInfoScreen()
        //screenNavigator.openGoodsInfoScreen()

        //screenNavigator.openGoodsListScreen()
        //screenNavigator.openLoadingTaskSettingsScreen()
    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen()

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}