package com.lenta.inventory.features.job_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil.convertTimeString
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class JobCardViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IInventoryTaskManager

    @Inject
    lateinit var storePlaceLockNetRequest: StorePlaceLockNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo

    private lateinit var tasksItem: TasksItem

    lateinit var typesRecount: List<RecountType>
    var recountsTitles: MutableLiveData<List<String>> = MutableLiveData(listOf())

    val title: String by lazy {
        "${tasksItem.taskType}-${tasksItem.taskNumber}"
    }

    val productType: ProductType? by lazy {
        if (tasksItem.gis == "A") ProductType.ExciseAlcohol else null
    }

    val stock: String by lazy {
        tasksItem.stock
    }

    val isPrimary: Boolean by lazy {
        tasksItem.isRecount.isBlank()
    }

    val actualPeriodFrom: String by lazy {
        convertTime(tasksItem.dateFrom)
    }


    val actualPeriodTo: String by lazy {
        convertTime(tasksItem.dateTo)
    }

    val isStrictList: Boolean by lazy {
        tasksItem.isStrict.isNotBlank()
    }

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)


    fun init(taskNumber: String, typesRecount: List<RecountType>, converterTypeToString: (RecountType) -> String) {

        tasksItem = repoInMemoryHolder.tasksListRestInfo.value?.tasks?.first { it.taskNumber == taskNumber }!!

        Logg.d { "taskItem $tasksItem" }

        this.typesRecount = typesRecount.filterIndexed { index, recountType ->
            var res: Boolean
            res = (if (isStrictList) true else index == 0) && (taskManager.getInventoryTask()?.taskDescription?.recountType
                    ?: recountType) == recountType
            if (res && (tasksItem.notFinish == "X" || tasksItem.mode == "2" || tasksItem.mode == "3")) {
                res = when (tasksItem.mode) {
                    "1" -> index == 0
                    "2" -> index == 1
                    "3" -> index == 2
                    else -> false
                }
            }
            res
        }

        this.recountsTitles.postValue(this.typesRecount
                .map {
                    converterTypeToString(it)
                })

    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position

    }

    fun onClickNext() {
        if (taskManager.getInventoryTask() == null || taskManager.getInventoryTask()?.taskDescription?.recountType != getSelectedTypeRecount()) {
            screenNavigator.openLoadingTaskContentsScreen(tasksItem, getSelectedTypeRecount()
                    ?: RecountType.None)
        } else {
            if (getSelectedTypeRecount() == RecountType.ParallelByStorePlaces) {
                screenNavigator.openStoragesList()
            } else {
                screenNavigator.openGoodsListScreen(storePlaceNumber = "00")
            }
        }

    }

    private fun getSelectedTypeRecount(): RecountType? {
        return if (typesRecount.size == 1) typesRecount[0] else typesRecount.getOrNull(selectedPosition.value
                ?: -1)
    }

    private fun convertTime(dateString: String): String {
        return convertTimeString(formatSource = Constants.DATE_FORMAT_yyyy_mm_dd, formatDestination = Constants.DATE_FORMAT_ddmmyy, date = dateString)
    }

    fun onBackPressed(): Boolean {
        if (taskManager.getInventoryTask() == null) {
            return true
        }

        screenNavigator.openConfirmationExitTask {
            unlockTask()
        }

        return false

    }

    private fun unlockTask() {
        viewModelScope.launch {
            screenNavigator.showProgress(storePlaceLockNetRequest)
            storePlaceLockNetRequest(StorePlaceLockParams(
                    ip = deviceInfo.getDeviceIp(),
                    taskNumber = tasksItem.taskNumber,
                    storePlaceCode = "00",
                    mode = StorePlaceLockMode.Unlock.mode,
                    userNumber = ""
            )).either(fnL = ::handleFailure) {
                Logg.d { "restInfo: $it" }
                taskManager.clearTask()
                screenNavigator.goBack()
                screenNavigator.hideProgress()
            }
        }
    }


    override fun handleFailure(failure: Failure) {
        screenNavigator.hideProgress()
        screenNavigator.openAlertScreen(failure)

    }


}
