package com.lenta.bp16.features.pack_good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.UnblockTaskNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PackGoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var unblockTaskNetRequest: UnblockTaskNetRequest


    val task by lazy {
        taskManager.currentTask
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
                        planWeight = "${good.planned} ${good.units.name}",
                        arrowVisibility = !task.isProcessed
                )
            }
        }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        val material = packGoods.value!![position].material
        task.value?.goods?.find { it.material == material }?.let { good ->
            if (good.raws.size > 1) {
                navigator.showMoreThanOneOrderForThisProduct()
                return
            }

            taskManager.currentGood.value = good
            taskManager.currentRaw.value = good.raws.find { it.material == good.material }
            navigator.openGoodPackagingScreen()
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            unblockTaskNetRequest(
                    UnblockTaskParams(
                            taskNumber = task.value!!.taskInfo.number,
                            unblockType = taskManager.getTaskTypeCode()
                    )
            )

            navigator.goBack()
        }
    }

}

data class ItemPackGoodListUi(
        val position: String,
        val material: String,
        val name: String,
        val planWeight: String,
        val arrowVisibility: Boolean
)