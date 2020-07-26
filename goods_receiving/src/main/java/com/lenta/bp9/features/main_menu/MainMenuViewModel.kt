package com.lenta.bp9.features.main_menu

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.platform.navigation.IScreenNavigator
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

    fun onClickShipmentTask() {
        screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Shipment)
    }

    fun onClickRecountTask() {
        screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.PGE)
    }

    fun onClickReceiptTask() {
        screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Receiving)
    }

    fun onClickUser() {
        screenNavigator.openSelectionPersonnelNumberScreen(isScreenMainMenu = true)

    }

    fun onClickAuxiliaryMenu() {
        screenNavigator.openAuxiliaryMenuScreen()
    }
}
