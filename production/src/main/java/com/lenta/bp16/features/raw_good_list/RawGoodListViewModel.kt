package com.lenta.bp16.features.raw_good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.UnblockTaskNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.coroutines.launch
import javax.inject.Inject

class RawGoodListViewModel : CoreViewModel() {

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
        "ЕО - ${task.processingUnit.number}"
    }

    val goods: MutableLiveData<List<ItemRawGoodListUi>> by lazy {
        MutableLiveData(task.goods!!.mapIndexed { index, good ->
            ItemRawGoodListUi(
                    position = (index + 1).toString(),
                    material = good.material,
                    name = "${good.material.takeLast(6)} ${good.name}",
                    arrived = "${good.planned} ${good.units.name}",
                    remain = "${(good.planned - good.getFactRawQuantity()).dropZeros()} ${good.units.name}"
            )
        })
    }

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
                            puNumber = task.processingUnit.number
                    )
            )

            navigator.goBack()
        }
    }

}

data class ItemRawGoodListUi(
        val position: String,
        val material: String,
        val name: String,
        val arrived: String,
        val remain: String
)