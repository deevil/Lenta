package com.lenta.bp16.features.pack_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class PackGoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager


    val task by lazy {
        taskManager.currentTask
    }

    val title by lazy {
        "ЕО - ${task.taskInfo.number}"
    }

    val packGoods: MutableLiveData<List<ItemPackGoodListUi>> by lazy {
        MutableLiveData(task.goods!!.mapIndexed { index, good ->
            ItemPackGoodListUi(
                    position = (index + 1).toString(),
                    material = good.material,
                    name = good.name,
                    planWeight = "${good.planned} ${good.units.name}",
                    arrowVisibility = !taskManager.currentTask.isProcessed
            )
        })
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        val material = packGoods.value!![position].material
        task.goods?.find { it.material == material }?.let { good ->
            taskManager.currentGood = good
            navigator.openGoodPackagingScreen()
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