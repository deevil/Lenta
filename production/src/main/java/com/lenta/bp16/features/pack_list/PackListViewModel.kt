package com.lenta.bp16.features.pack_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    val title by lazy {
        taskManager.currentGood.getNameWithMaterial(" - ")
    }

    val packs: MutableLiveData<List<ItemPackListUi>> by lazy {
        MutableLiveData(taskManager.currentGood.packs.mapIndexed { index, pack ->
            ItemPackListUi(
                    position = (index + 1).toString(),
                    number = "Тара №${pack.getShortPackNumber()}",
                    name = taskManager.currentRaw.name,
                    weight = "${pack.quantity.dropZeros()} ${taskManager.currentGood.units.name}"
            )
        })
    }

    // -----------------------------

    fun onClickAdd() {
        navigator.goBack()
    }

    fun onClickComplete() {
        navigator.showDefrostingPhaseIsCompleted {
            taskManager.currentGood.isProcessed = true

            navigator.goBack()
            navigator.goBack()
        }
    }

}

data class ItemPackListUi(
        val position: String,
        val number: String,
        val name: String,
        val weight: String
)