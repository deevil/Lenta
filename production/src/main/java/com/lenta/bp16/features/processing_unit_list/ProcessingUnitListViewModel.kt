package com.lenta.bp16.features.processing_unit_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.EndProcessingNetRequest
import com.lenta.bp16.request.EndProcessingParams
import com.lenta.bp16.request.UnblockTaskNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class ProcessingUnitListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var manager: ITaskManager
    @Inject
    lateinit var unblockTaskNetRequest: UnblockTaskNetRequest
    @Inject
    lateinit var endProcessingNetRequest: EndProcessingNetRequest


    private val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { it?.taskInfo?.text3 }
    }

    val goods: MutableLiveData<List<ItemProcessingUnitUi>> by lazy {
        task.map { task ->
            task?.goods!!.mapIndexed { index, good ->
                ItemProcessingUnitUi(
                        position = (index + 1).toString(),
                        material = good.material,
                        name = "${good.material.takeLast(6)} ${good.name}",
                        arrived = "${good.arrived.dropZeros()} ${good.units.name}",
                        remain = "${(good.arrived - good.getPackedQuantity()).dropZeros()} ${good.units.name}"
                )
            }
        }
    }

    val completeEnabled by lazy {
        task.map { task ->
            task?.isProcessed == false && task.goods.map {
                it.getPackedQuantity()
            }.any { it > 0.0 }
        }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        val material = goods.value!![position].material
        task.value?.goods?.first { it.material == material }?.let { good ->
            manager.updateCurrentGood(good)
            navigator.openRawListScreen()
        }
    }

    fun onBackPressed() {
        launchUITryCatch {
            unblockTaskNetRequest(
                    UnblockTaskParams(
                            taskNumber = task.value!!.taskInfo.number,
                            unblockType = manager.getTaskTypeCode()
                    )
            )

            navigator.goBack()
        }
    }

    fun onClickComplete() {
        navigator.showConfirmNoRawItem(manager.taskType.abbreviation) {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)

                endProcessingNetRequest(
                        EndProcessingParams(
                                taskNumber = task.value!!.taskInfo.number,
                                taskType = manager.getTaskTypeCode()
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    manager.completeCurrentTask()
                    navigator.goBack()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

}

data class ItemProcessingUnitUi(
        val position: String,
        val material: String,
        val name: String,
        val arrived: String,
        val remain: String
)