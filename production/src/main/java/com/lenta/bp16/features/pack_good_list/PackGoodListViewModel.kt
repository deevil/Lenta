package com.lenta.bp16.features.pack_good_list

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
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PackGoodListViewModel : CoreViewModel() {

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

    val packGoods: MutableLiveData<List<ItemPackGoodListUi>> by lazy {
        task.map { task ->
            task?.goods?.mapIndexed { index, good ->
                ItemPackGoodListUi(
                        position = (index + 1).toString(),
                        material = good.material,
                        name = good.name,
                        arrived = "${good.arrived.dropZeros()} ${good.units.name}",
                        remain = "${(good.arrived - good.getPackedQuantity()).dropZeros()} ${good.units.name}",
                        arrowVisibility = !task.isProcessed
                )
            }
        }
    }

    val completeEnabled by lazy {
        task.map { task ->
            task?.isProcessed == false && !task.goods.any { it.packs.isEmpty() }
        }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            checkTaskForCorrectness()
        }
    }

    // -----------------------------

    private fun checkTaskForCorrectness() {
        task.value!!.let { task ->
            if (task.goods.any { it.raws.size > 1 }) {
                navigator.showMoreThanOneOrderForThisProduct {
                    onBackPressed()
                }
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        if (task.value?.isProcessed == true) {
            return
        }

        val material = packGoods.value!![position].material
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let { good ->
                manager.updateCurrentGood(good)
                manager.updateCurrentRaw(good.raws.find { it.material == good.material })
                navigator.openGoodPackagingScreen()
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
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
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                endProcessingNetRequest(
                        EndProcessingParams(
                                taskNumber = manager.currentTask.value!!.number,
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

data class ItemPackGoodListUi(
        val position: String,
        val material: String,
        val name: String,
        val arrived: String,
        val remain: String,
        val arrowVisibility: Boolean
)