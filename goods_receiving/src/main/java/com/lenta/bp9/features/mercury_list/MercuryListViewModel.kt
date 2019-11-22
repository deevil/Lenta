package com.lenta.bp9.features.mercury_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class MercuryListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val tiedSelectionsHelper = SelectionItemsHelper()
    val untiedSelectionsHelper = SelectionItemsHelper()
    val listTied: MutableLiveData<List<MercuryListItem>> = MutableLiveData()
    val listUntied: MutableLiveData<List<MercuryListItem>> = MutableLiveData()

    val nextEnabled = listUntied.map {
        it?.size == 0
    }

    val tiedEnabled: MutableLiveData<Boolean> = untiedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = untiedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val untiedEnabled: MutableLiveData<Boolean> = tiedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = tiedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onResume() {
        updateListTied()
        updateListUntied()
    }

    private fun updateListTied() {
        taskManager.getReceivingTask()?.let { task ->
            listTied.postValue(listOf(
                    MercuryListItem(
                            number = 1,
                            name = "test1",
                            quantityWithUom = "10 kg",
                            isCheck = false,
                            even = true),
                    MercuryListItem(
                            number = 2,
                            name = "test2",
                            quantityWithUom = "20 kg",
                            isCheck = true,
                            even = true)
            )
            )
        }
    }

    private fun updateListUntied() {
        /**taskManager.getReceivingTask()?.let { task ->
            listUntied.postValue(listOf(
                    MercuryListItem(
                            number = 1,
                            name = "tied1",
                            quantityWithUom = "30 kg",
                            isCheck = false,
                            even = true),
                    MercuryListItem(
                            number = 2,
                            name = "tied2",
                            quantityWithUom = "40 kg",
                            isCheck = true,
                            even = true)
            )
            )
        }*/
    }


    fun onClickItemPosition(position: Int) {
        /**val matnr: String?
        if (selectedPage.value == 0) {
            matnr = listCounted.value?.get(position)?.productInfo?.materialNumber
        } else {
            matnr = listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
        }
        matnr?.let {
            val productInfo = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.findProduct(it)
            if (productInfo != null) searchProductDelegate.openProductScreen(productInfo, false)
        }*/
    }

    fun onClickTiedUntied() {
        return
    }

    fun onClickNext() {
        return
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}
