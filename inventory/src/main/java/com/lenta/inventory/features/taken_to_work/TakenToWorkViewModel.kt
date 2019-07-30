package com.lenta.inventory.features.taken_to_work

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getInvCountDuration
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.date_time.DateTimeUtil.convertMilisecondsToHHMm
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TakenToWorkViewModel : CoreViewModel() {
    @Inject
    lateinit var taskManager: IInventoryTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var hyperHive: HyperHive

    private val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

    val titleTask = MutableLiveData("")
    val timeCount = MutableLiveData("")

    init {
        viewModelScope.launch {
            val timeInMillis: Long = withContext(Dispatchers.IO) {
                ((zmpUtz14V001.getInvCountDuration()?.toFloatOrNull()
                        ?: 0F) * 3600 * 1000).toLong()
            }

            timeCount.value = convertMilisecondsToHHMm(timeInMillis)

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
            }
        }
    }


}
