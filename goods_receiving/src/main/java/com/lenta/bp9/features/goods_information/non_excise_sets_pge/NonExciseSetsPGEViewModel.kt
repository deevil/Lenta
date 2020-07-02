package com.lenta.bp9.features.goods_information.non_excise_sets_pge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.ListWithoutBarcodeItem
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseSetsPGEProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class NonExciseSetsPGEViewModel : CoreViewModel(),
        PageSelectionListener,
        OnOkInSoftKeyboardListener,
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var processNonExciseSetsPGEProductService: ProcessNonExciseSetsPGEProductService

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val componentsSelectionsHelper = SelectionItemsHelper()
    val listComponents: MutableLiveData<List<ListWithoutBarcodeItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + countAccept
            } else {
                countAccept
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            it == 0.0 -> {
                "0 ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code != "1") {
                (it?.first ?: 0.0) + countRefusal
            } else {
                countRefusal
            }
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val enabledCleanButton: MutableLiveData<Boolean> = componentsSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@NonExciseSetsPGEViewModel::viewModelScope,
                    scanResultHandler = this@NonExciseSetsPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.uom?.name
            if (isDiscrepancy.value!!) {
                count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo.value!!).toStringFormatted()
                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "4"}
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            if (processNonExciseSetsPGEProductService.newProcessNonExciseSetsPGEProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    fun onClickClean() {

    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {

    }

    fun onClickApply() {

    }

    fun onClickItemPosition(position: Int) {
        /**val matnr: String? = if (selectedPage.value == 0) {
            if (taskType.value == TaskType.ShipmentPP) {
                listToProcessing.value?.get(position)?.productInfo?.materialNumber
            } else {
                listCounted.value?.get(position)?.productInfo?.materialNumber
            }
        } else {
            if (taskType.value == TaskType.ShipmentPP) {
                listProcessed.value?.get(position)?.productInfo?.materialNumber
            } else {
                listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
            }
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false)*/
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onScanResult(data: String) {
        /**addGoods.value = false
        onClickAdd()
        if (addGoods.value == true) {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }*/
    }

    fun onBackPressed() {
        if (enabledApplyButton.value == true) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

}

data class ListComponentsItem(
        val number: Int,
        val name: String,
        val nameMaxLines: Int,
        val nameBatch: String,
        val visibilityNameBatch: Boolean,
        val countAcceptWithUom: String,
        val countRefusalWithUom: String,
        val isNotEdit: Boolean,
        val productInfo: TaskProductInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}
