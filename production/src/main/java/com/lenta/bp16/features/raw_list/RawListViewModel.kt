package com.lenta.bp16.features.raw_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.coroutines.launch
import javax.inject.Inject

class RawListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    val good by lazy {
        taskManager.currentGood
    }

    val title by lazy {
        good.getNameWithMaterial()
    }

    val description by lazy {
        taskManager.currentTask.taskInfo.text3
    }

    val raw = MutableLiveData<List<ItemRawListUi>>()

    val completeEnabled = MutableLiveData(true)

    // -----------------------------

    init {
        viewModelScope.launch {
            updateList()
        }
    }

    // -----------------------------

    fun updateList() {
        raw.value = taskManager.currentGood.raws.mapIndexed { index, raw ->
            ItemRawListUi(
                    position = (index + 1).toString(),
                    materialOsn = raw.materialOsn,
                    name = raw.name,
                    processed = "${raw.quantity.dropZeros()} ${good.units.name} из ${raw.planned.dropZeros()} ${good.units.name}",
                    arrowVisibility = !taskManager.currentTask.isProcessed
            )
        }
    }

    fun onClickComplete() {
        navigator.showConfirmNoSuchItemLeft(taskManager.taskType.abbreviation) {
            good.isProcessed = true
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        val materialOsn = raw.value!![position].materialOsn
        good.raws.find { it.materialOsn == materialOsn }?.let { raw ->
            taskManager.currentRaw = raw
            navigator.openGoodWeighingScreen()
        }
    }

}

data class ItemRawListUi(
        val position: String,
        val materialOsn: String,
        val name: String,
        val processed: String,
        val arrowVisibility: Boolean
)