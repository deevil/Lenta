package com.lenta.bp16.features.raw_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
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
        "ЕО - ${taskManager.currentTask.taskInfo.number}"
    }

    val raws: MutableLiveData<List<ItemRawListUi>> by lazy {
        MutableLiveData(good.raws.mapIndexed { index, raw ->
            ItemRawListUi(
                    position = (index + 1).toString(),
                    materialOsn = raw.materialOsn,
                    name = raw.name,
                    processed = "${raw.totalQuantity} ${good.units.name} из ${raw.planned} ${good.units.name}",
                    arrowVisibility = !taskManager.currentTask.isProcessed
            )
        })
    }

    val completeEnabled = MutableLiveData(true)

    // -----------------------------

    fun onClickComplete() {
        navigator.showConfirmNoSuchItemLeft(taskManager.taskType.name) {
            good.isProcessed = true
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        val materialOsn = raws.value!![position].materialOsn
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