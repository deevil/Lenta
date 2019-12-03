package com.lenta.bp16.features.external_supply_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.extention.getTaskType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.EndProcessingNetRequest
import com.lenta.bp16.request.EndProcessingParams
import com.lenta.bp16.request.UnblockTaskNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExternalSupplyListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var unblockTaskNetRequest: UnblockTaskNetRequest
    @Inject
    lateinit var endProcessingNetRequest: EndProcessingNetRequest


    val task by lazy {
        taskManager.currentTask
    }

    val title by lazy {
        task.taskInfo.text3
    }

    val goods: MutableLiveData<List<ItemExternalSupplyUi>> by lazy {
        MutableLiveData(task.goods!!.mapIndexed { index, good ->
            ItemExternalSupplyUi(
                    position = (index + 1).toString(),
                    material = good.material,
                    name = "${good.material.takeLast(6)} ${good.name}",
                    arrived = "${good.planned} ${good.units.name}"
            )
        })
    }

    val completeEnabled = MutableLiveData(true)

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        val material = goods.value!![position].material
        task.goods?.first { it.material == material }?.let { good ->
            taskManager.currentGood = good
            navigator.openRawListScreen()
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            unblockTaskNetRequest(
                    UnblockTaskParams(
                            taskNumber = task.taskInfo.number,
                            unblockType = taskManager.taskType.getTaskType()
                    )
            )

            navigator.goBack()
        }
    }

    fun onClickComplete() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            endProcessingNetRequest(
                    EndProcessingParams(
                            taskNumber = task.taskInfo.number,
                            taskType = taskManager.taskType.getTaskType()
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                completeTask()
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun completeTask() {
        task.isProcessed = true
        navigator.goBack()
    }

}

data class ItemExternalSupplyUi(
        val position: String,
        val material: String,
        val name: String,
        val arrived: String
)