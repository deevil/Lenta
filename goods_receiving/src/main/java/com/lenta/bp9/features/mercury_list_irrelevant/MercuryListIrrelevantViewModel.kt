package com.lenta.bp9.features.mercury_list_irrelevant

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.toStringFormatted
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
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getMercuryNotActual().let {listMercuryNotActual ->
            listIrrelevantMercury.postValue(
                    listMercuryNotActual?.mapIndexed { index, mercuryNotActual ->
                        MercuryListIrrelevantItem(
                                number = index + 1,
                                name = "${mercuryNotActual.getMaterialLastSix()} ${mercuryNotActual.productName}",
                                quantityWithUom = "- ${mercuryNotActual.volume.toStringFormatted()} ${mercuryNotActual.uom.name}",
                                even = index % 2 == 0)
                    }
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
