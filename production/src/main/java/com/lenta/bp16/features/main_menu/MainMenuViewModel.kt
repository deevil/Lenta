package com.lenta.bp16.features.main_menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val fio = MutableLiveData("")

    // -----------------------------

    init {
        viewModelScope.launch {
            fio.value = sessionInfo.personnelFullName
            manager.getLabelList()
        }
    }

    // -----------------------------

    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickExternalSupply() {
        manager.taskType = TaskType.EXTERNAL_SUPPLY
        navigator.openExternalSupplyTaskListScreen()
    }

    fun onClickProcessingUnit() {
        manager.taskType = TaskType.PROCESSING_UNIT
        navigator.openProcessingUnitTaskListScreen()
    }

    fun onClickUser() {
        navigator.openSelectionPersonnelNumberScreen()
    }

}