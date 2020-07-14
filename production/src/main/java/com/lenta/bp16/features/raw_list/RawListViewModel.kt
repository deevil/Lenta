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
    lateinit var manager: ITaskManager


    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val description by lazy {
        manager.currentTask.map { it?.taskInfo?.text3 }
    }

    val raws: MutableLiveData<List<ItemRawListUi>> by lazy {
        good.map { good ->
            good?.raws?.mapIndexed { index, raw ->
                val packed = good.packs.filter {
                    it.isNotDefect() && it.order == raw.order
                }.map { it.quantity }.sumList()

                ItemRawListUi(
                        position = (index + 1).toString(),
                        materialOsn = raw.materialOsn,
                        order = raw.order,
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
        navigator.showConfirmNoSuchItemLeft(manager.taskType.abbreviation) {
            manager.completeCurrentGood()
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        if (good.value?.isProcessed == true) {
            return
        }

        val order = raws.value!![position].order
        good.value?.raws?.find { it.order == order }?.let { raw ->
            manager.updateCurrentRaw(raw)
            navigator.openGoodWeighingScreen()
        }
    }

}

data class ItemRawListUi(
        val position: String,
        val materialOsn: String,
        val order: String,
        val name: String,
        val processingStatus: String,
        val arrowVisibility: Boolean
)