package com.lenta.inventory.features.taken_to_work

import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class TakenToWorkViewModel : CoreViewModel() {
    @Inject
    lateinit var taskManager: IInventoryTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val titleTask = MutableLiveData("")
    val timeCount = MutableLiveData("")

    init {
        launchUITryCatch {

            timeCount.value = taskManager.getInventoryTask()?.getElapsedTimePrintable(timeMonitor.getUnixTime())

            taskManager.getInventoryTask()?.taskDescription?.let {
                titleTask.value = "${it.taskType}-${it.taskNumber}"
            }
        }
    }


    fun onClickNext() {
        taskManager.getInventoryTask()?.taskDescription?.let {
            screenNavigator.goBack()
            when (it.recountType) {
                RecountType.Simple, RecountType.ParallelByPerNo -> {
                    screenNavigator.openGoodsListScreen("00")
                }
                RecountType.ParallelByStorePlaces -> screenNavigator.openStoragesList()
                else -> Unit
            }
        }
    }


}
