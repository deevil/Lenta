package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class ExciseAlcoBoxListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val countNotProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<BoxListItem>> = MutableLiveData()
    val processedSelectionsHelper = SelectionItemsHelper()
    val scanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToScanCode: MutableLiveData<Boolean> = MutableLiveData()

    fun onResume() {
        updateData()
    }

    private fun updateData() {
        val boxNotProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.getBoxes()
        val boxProcessed = taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.getBoxesDiscrepancies()

        boxNotProcessed?.let {boxInfoList ->
            countNotProcessed.postValue(
                    boxInfoList
                            .filter {
                                taskManager.getReceivingTask()?.taskRepository?.getBoxesDiscrepancies()?.findDiscrepanciesOfBox(it)?.filter { foundDiscrepancies ->
                                    foundDiscrepancies.boxNumber == it.boxNumber
                                }?.size == 0
                            }
                            .mapIndexed { index, boxInfo ->
                                BoxListItem(
                                        number = index + 1,
                                        name = "${boxInfo.boxNumber.substring(0,10)} ${boxInfo.boxNumber.substring(10,20)} ${boxInfo.boxNumber.substring(20,26)}",
                                        productInfo = productInfo.value,
                                        productDiscrepancies = null,
                                        boxInfo = boxInfo,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }

        boxProcessed?.let {boxDiscrepanciesList ->
            countProcessed.postValue(
                    boxDiscrepanciesList
                            .mapIndexed { index, boxDiscrepancies ->
                                BoxListItem(
                                        number = index + 1,
                                        name = "${boxDiscrepancies.boxNumber.substring(0,10)} ${boxDiscrepancies.boxNumber.substring(10,20)} ${boxDiscrepancies.boxNumber.substring(20,26)}",
                                        productInfo = productInfo.value,
                                        productDiscrepancies = null,
                                        boxInfo = null,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }
    }

    fun onClickSecondBtn(){
        if (selectedPage.value == 0) {
            Logg.d { "testddi ${selectedPage.value}" }
        } else {
            Logg.d { "testddi ${selectedPage.value}" }
        }
    }

    fun onClickHandleGoods(){
    }

    fun onClickApply(){
        screenNavigator.openExciseAlcoBoxCardScreen(productInfo.value!!)
    }

    fun onClickItemPosition(position: Int) {
        /**val matnr: String? = if (selectedPage.value == 0) {
            countNotProcessed.value?.get(position)?.productInfo?.materialNumber
        } else {
            countProcessed.value?.get(position)?.productInfo?.materialNumber
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false, isDiscrepancy = true)*/
    }

    override fun onOkInSoftKeyboard(): Boolean {
        /**eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }*/
        return true
    }

    fun onScanResult(data: String) {
        //searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}

data class BoxListItem(
        val number: Int,
        val name: String,
        val productInfo: TaskProductInfo?,
        val productDiscrepancies: TaskProductDiscrepancies?,
        val boxInfo: TaskBoxInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}
