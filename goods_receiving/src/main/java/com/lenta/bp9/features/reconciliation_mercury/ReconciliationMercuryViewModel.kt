package com.lenta.bp9.features.reconciliation_mercury

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class ReconciliationMercuryViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val sapNameGoods: MutableLiveData<String> = MutableLiveData("")
    val mercuryNameGoods: MutableLiveData<String> = MutableLiveData("")
    val ligamentType: MutableLiveData<String> = MutableLiveData("")
    val reconciliationCheck: MutableLiveData<Boolean> = MutableLiveData(false)

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        sapNameGoods.value = "sapNameGoods"
        mercuryNameGoods.value = "mercuryNameGoods"
        ligamentType.value = "ligamentType"
    }
}
