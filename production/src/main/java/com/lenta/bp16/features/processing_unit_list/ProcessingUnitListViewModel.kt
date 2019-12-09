package com.lenta.bp16.features.processing_unit_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.EndProcessingNetRequest
import com.lenta.bp16.request.EndProcessingParams
import com.lenta.bp16.request.UnblockTaskNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProcessingUnitListViewModel : CoreViewModel() {

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

    val goods = MutableLiveData<List<ItemProcessingUnitUi>>()

    val completeEnabled = MutableLiveData(true)

    // -----------------------------

    init {
        viewModelScope.launch {
            updateList()
        }
    }

    // -----------------------------

    fun updateList() {
        goods.value = taskManager.currentTask.goods!!.mapIndexed { index, good ->
            ItemProcessingUnitUi(
                    position = (index + 1).toString(),
                    material = good.material,
                    name = "${good.material.takeLast(6)} ${good.name}",
                    arrived = "${good.planned.dropZeros()} ${good.units.name}",
                    remain = "${(good.planned - good.getFactRawQuantity()).dropZeros()} ${good.units.name}"
            )
        }
    }

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
                            unblockType = taskManager.getTaskType()
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
                            taskType = taskManager.getTaskType()
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

data class ItemProcessingUnitUi(
        val position: String,
        val material: String,
        val name: String,
        val arrived: String,
        val remain: String
)