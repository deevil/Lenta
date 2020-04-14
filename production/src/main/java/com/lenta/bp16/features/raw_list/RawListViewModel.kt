package com.lenta.bp16.features.raw_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumList
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
        good.map { it?.getNameWithMaterial() }
    }

    val description by lazy {
        taskManager.currentTask.map { it?.taskInfo?.text3 }
    }

    val raws: MutableLiveData<List<ItemRawListUi>> by lazy {
        good.map { good ->
            good?.raws?.mapIndexed { index, raw ->
                val packed = good.packs.filter {
                    it.isNotDefect() && it.orderNumber == raw.orderNumber
                }.map { it.quantity }.sumList()

                ItemRawListUi(
                        position = (index + 1).toString(),
                        materialOsn = raw.materialOsn,
                        name = raw.name,
                        processingStatus = "${packed.dropZeros()} ${good.units.name} из ${raw.planned.dropZeros()} ${good.units.name}",
                        arrowVisibility = !good.isProcessed
                )
            }
        }
    }

    val completeEnabled by lazy {
        good.map { good ->
            val isAllWeighted = good?.raws?.map { raw -> good.packs.filter {
                    it.materialOsn == raw.materialOsn
                }.map { it.quantity }.sum() }?.find { it == 0.0 }?.let { false } ?: true

            good?.isProcessed == false && isAllWeighted
        }
    }

    // -----------------------------

    fun onClickComplete() {
        navigator.showConfirmNoSuchItemLeft(taskManager.taskType.abbreviation) {
            taskManager.completeCurrentGood()
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        if (good.value?.isProcessed == true) {
            return
        }

        val materialOsn = raws.value!![position].materialOsn
        good.value?.raws?.find { it.materialOsn == materialOsn }?.let { raw ->
            taskManager.currentRaw.value = raw
            navigator.openGoodWeighingScreen()
        }
    }

}

data class ItemRawListUi(
        val position: String,
        val materialOsn: String,
        val name: String,
        val processingStatus: String,
        val arrowVisibility: Boolean
)