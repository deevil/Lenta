package com.lenta.bp9.features.mercury_list_irrelevant

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MercuryListIrrelevantViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listIrrelevantMercury: MutableLiveData<List<MercuryListIrrelevantItem>> = MutableLiveData()

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onResume() {
        updateListIrrelevant()
    }

    private fun updateListIrrelevant() {
        taskManager.getReceivingTask()?.let { task ->
            listIrrelevantMercury.postValue(listOf(
                    MercuryListIrrelevantItem(
                            number = 1,
                            name = "test1",
                            quantityWithUom = "10 kg",
                            even = true),
                    MercuryListIrrelevantItem(
                            number = 2,
                            name = "test2",
                            quantityWithUom = "20 kg",
                            even = true)
                    ,
                    MercuryListIrrelevantItem(
                            number = 3,
                            name = "test2",
                            quantityWithUom = "30 kg",
                            even = true)
            )
            )
        }
    }

    fun onClickUntied() {
        return
    }

    fun onClickTemporary() {
        return
    }
}
