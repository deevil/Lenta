package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class ExciseAlcoBoxListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val countNotProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val processedSelectionsHelper = SelectionItemsHelper()

    fun onClickItemPosition(position: Int) {
        /**val matnr: String? = if (selectedPage.value == 0) {
            countNotProcessed.value?.get(position)?.productInfo?.materialNumber
        } else {
            countProcessed.value?.get(position)?.productInfo?.materialNumber
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false, isDiscrepancy = true)*/
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}

data class BoxListItem(
        val number: Int,
        val name: String,
        val countRefusalWithUom: String,
        val quantityNotProcessedWithUom: String,
        val discrepanciesName: String,
        val isNormDiscrepancies: Boolean,
        val productInfo: TaskProductInfo?,
        val productDiscrepancies: TaskProductDiscrepancies?,
        val batchInfo: TaskBatchInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
