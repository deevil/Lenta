package com.lenta.bp9.features.goods_information.general_opp

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessGeneralProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsInfoShipmentPPViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processGeneralProductService: ProcessGeneralProductService
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var context: Context

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    val count: MutableLiveData<String> = MutableLiveData("0")
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code)
    }
    val tvTotal: MutableLiveData<String> by lazy {
        if (isEizUnit.value == true) {
            MutableLiveData(context.getString(R.string.total_param, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
        } else {
            MutableLiveData(context.getString(R.string.total))
        }

    }
    val ipPlanWithUom: MutableLiveData<String> by lazy {
        MutableLiveData("${productInfo.value!!.origQuantity.toDouble().toStringFormatted()} ${productInfo.value!!.uom.name}")
    }
    val orderPlanWithUom: MutableLiveData<String> by lazy {
        MutableLiveData("${productInfo.value!!.orderQuantity.toDouble().toStringFormatted()} ${productInfo.value!!.uom.name}")
    }

    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    private val totalCount: MutableLiveData<Double> = countValue.map {
        //если нажимаем Применить, то записывается с typeDiscrepancies=1, а если нажимаем Отсутствует, то с typeDiscrepancies=1 и кол-вом 0, поэтому по getCountRefusalOfProduct всегда будет ноль, поэтому всегда берем getCountAcceptOfProduct
        if ((it ?: 0.0) > 0.0) {
            (it ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        } else {
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        }
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val enabledMissingButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) <= 0.0
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@GoodsInfoShipmentPPViewModel::viewModelScope,
                    scanResultHandler = this@GoodsInfoShipmentPPViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.uom?.name

            if (isDiscrepancy.value!!) {
                count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo.value!!).toStringFormatted()
            }

            spinQuality.value = listOf(context.getString(R.string.quantity))

            if (processGeneralProductService.newProcessGeneralProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickApply() {
        if (processGeneralProductService.overLimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimit()
            count.value = "0"
        } else {
            processGeneralProductService.add(totalCount.value!!.toString(), "1")
            screenNavigator.goBack()
        }
    }

    fun onClickMissing() {
        processGeneralProductService.add("0", "3")
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        if (enabledApplyButton.value == true) {
            onClickApply()
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
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

    override fun onClickPosition(position: Int) {
        spinQualitySelectedPosition.value = position
    }

}


