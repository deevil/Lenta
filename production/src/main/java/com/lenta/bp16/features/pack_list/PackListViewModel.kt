package com.lenta.bp16.features.pack_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val packs: MutableLiveData<List<ItemPackListUi>> by lazy {
        good.map { good ->
            good?.packs?.let { packs ->
                packs.mapIndexed { index, pack ->
                    ItemPackListUi(
                            position = (packs.size - index).toString(),
                            number = "Тара №${pack.getShortPackNumber()}",
                            name = raw.value!!.name,
                            weight = "${pack.quantity.dropZeros()} ${good.units.name}"
                    )
                }
            }
        }
    }

    // -----------------------------

    fun onClickAdd() {
        navigator.goBack()
    }

    fun onClickComplete() {
        navigator.showDefrostingPhaseIsCompleted {
            taskManager.completeCurrentGood()

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