package com.lenta.bp9.features.discrepancy_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val processedSelectionsHelper = SelectionItemsHelper()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()


    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = processedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> = MutableLiveData()

    init {
        countNotProcessed.postValue(listOf(GoodsDiscrepancyItem(
                number = 1,
                name = "qwerty",
                quantity = "5",
                even = true
        )))
        countProcessed.postValue(listOf(GoodsDiscrepancyItem(
                number = 2,
                name = "asdfg",
                quantity = "50",
                even = true
        )))
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        //todo
        /**if (selectedPage.value == 0) {
        countedGoods.value?.getOrNull(position)?.productInfo
        } else {
        filteredGoods.value?.getOrNull(position)?.productInfo
        }?.let {
        searchProductDelegate.openProductScreen(it, 0.0)
        }*/
    }

    fun onClickGoodsTitle(position: Int) {
        //todo
        return
    }
}
