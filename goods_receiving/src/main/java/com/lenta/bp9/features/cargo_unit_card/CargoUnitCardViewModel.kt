package com.lenta.bp9.features.cargo_unit_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class CargoUnitCardViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val cargoUnitInfo: MutableLiveData<TaskCargoUnitInfo> = MutableLiveData()
    val spinStatus: MutableLiveData<List<String>> = MutableLiveData()
    val spinStatusSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinTypePallet: MutableLiveData<List<String>> = MutableLiveData()
    val spinTypePalletSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val recountValue: MutableLiveData<String> = MutableLiveData()

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }
}
