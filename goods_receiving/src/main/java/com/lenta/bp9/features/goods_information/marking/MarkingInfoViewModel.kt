package com.lenta.bp9.features.goods_information.marking

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.non_excise_sets_receiving.NonExciseSetsReceivingViewModel
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

//https://trello.com/c/NGsFfWgB
class MarkingInfoViewModel : CoreViewModel(),
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var hyperHive: HyperHive

    val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val tvAccept: MutableLiveData<String> = MutableLiveData("")
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    private val countExciseStampsScanned: MutableLiveData<Int> = MutableLiveData(0)
    val spinQualityEnabled: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        it == 0
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData()
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val paramGrzMeinsPack: MutableLiveData<String> = MutableLiveData("")
    private val isBlockMode: MutableLiveData<Boolean> = MutableLiveData(false)
    private val uom: MutableLiveData<Uom> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map {
        val nestingInOneBlock = productInfo.value?.nestingInOneBlock?.toDouble() ?: 0.0
        if (isBlockMode.value == true) {
            (it?.toDoubleOrNull() ?: 0.0) * nestingInOneBlock
        } else {
            it?.toDoubleOrNull() ?: 0.0
        }
    }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountAccept = productInfo.value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountAcceptOfProduct(product)
                }
                ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo.value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountAccept
                }
                ?: productCountAccept
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val productCountAccept = productInfo.value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountAcceptOfProduct(product)
                }
                ?: 0.0
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
            }
            else -> { //если было введено отрицательное значение
                "${if (productCountAccept > 0.0) "+ " + productCountAccept.toStringFormatted() else productCountAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountRefusal = productInfo.value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountRefusalOfProduct(product)
                }
                ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo.value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountRefusal
                }
                ?: productCountRefusal
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val productCountRefusal = productInfo.value
                ?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountRefusalOfProduct(product)
                }
                ?: 0.0

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
        } else { //если было введено отрицательное значение
            "${if (productCountRefusal > 0.0) "- " + productCountRefusal.toStringFormatted() else productCountRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countExciseStampsScanned)
            .map {
                val acceptTotalCountVal = acceptTotalCount.value ?: 0.0
                val countExciseStampsScannedVal = countExciseStampsScanned.value ?: 0
                val qualityInfoCode = qualityInfo.value?.get(it?.second ?: 0)?.code.orEmpty()
                val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
                if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
                        checkStampControlVisibility.value = false
                        context.getString(R.string.not_required)
                    } else {
                        checkStampControlVisibility.value = true
                        if (acceptTotalCountVal < numberStampsControl) {
                            "${countExciseStampsScannedVal.toDouble().toStringFormatted()} ${context.getString(R.string.of)} ${acceptTotalCountVal.toStringFormatted()}"
                        } else {
                            "${countExciseStampsScannedVal.toDouble().toStringFormatted()} ${context.getString(R.string.of)} ${numberStampsControl.toStringFormatted()}"
                        }
                    }
                } else {
                    "" //это поле отображается только при выбранной категории "Норма"
                }
            }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        productInfo.value?.let {
            taskManager.getReceivingTask()?.controlExciseStampsOfProduct(it)
        } ?: false
    }

    val checkBoxGtinControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvMrcVal: MutableLiveData<String> = acceptTotalCount.combineLatest(spinQualitySelectedPosition).map {
        val acceptTotalCountValue = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        val productNumberBoxesControl = productInfo.value?.numberBoxesControl?.toDouble() ?: 0.0
        val productNumberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0

        if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            if ((productNumberBoxesControl == 0.0 && productNumberStampsControl == 0.0) || acceptTotalCountValue <= 0.0) {
                checkBoxGtinControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkBoxGtinControlVisibility.value = true
                val countBoxesPassedControlOfProductValue = productInfo.value?.let { product ->
                    taskManager
                            .getReceivingTask()
                            ?.countBoxesPassedControlOfProduct(product)
                } ?: 0
                if (acceptTotalCountValue < productNumberBoxesControl) {
                    "$countBoxesPassedControlOfProductValue ${context.getString(R.string.of)} ${acceptTotalCountValue.toStringFormatted()}"
                } else {
                    "$countBoxesPassedControlOfProductValue ${context.getString(R.string.of)} ${productNumberBoxesControl.toStringFormatted()}"
                }
            }
        } else "" //это поле отображается только при выбранной категории "Норма"
    }

    val checkBoxGtinControl: MutableLiveData<Boolean> = checkBoxGtinControlVisibility.map {
        productInfo.value?.let {
            taskManager
                    .getReceivingTask()
                    ?.controlBoxesOfProduct(it)
        } ?: false
    }

    val checkBoxListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvBoxListVal: MutableLiveData<String> = refusalTotalCount.combineLatest(spinQualitySelectedPosition).map {
        val refusalTotalCountValue = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        if (qualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            "test"
            /**if (refusalTotalCountValue == 0.0 || refusalTotalCountValue.toInt() == processExciseAlcoBoxAccService.getCountUntreatedBoxes()) {
                checkBoxListVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkBoxListVisibility.value = true
                "${processExciseAlcoBoxAccService.getCountDefectBoxes()} ${context.getString(R.string.of)} ${refusalTotalCountValue.toStringFormatted()}"
            }*/
        } else "" //это поле отображается только при выбранной категории брака
    }

    val checkBoxList: MutableLiveData<Boolean> = checkBoxListVisibility.map {
        //https://trello.com/c/lqyZlYQu, Устанавливать чекбокс, когда F (кол-во в Отказать) = Q (свободные/необработанные короба);
        //refusalTotalCount.value == processExciseAlcoBoxAccService.getCountDefectBoxes().toDouble()
        false
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(checkBoxList).map {
        val totalCount = countValue.value ?: 0.0
        val spinQualitySelectedPositionValue = spinQualitySelectedPosition.value ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code
        if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            totalCount != 0.0
        } else {
            /**val someMinusValue = processExciseAlcoBoxAccService.getCountUntreatedBoxes() - totalCount.toInt()
            (someMinusValue == 0 || it!!.second) && (totalCount > 0.0)*/
            false
        }
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        (it ?: 0) > 0
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@MarkingInfoViewModel::viewModelScope,
                    scanResultHandler = this@MarkingInfoViewModel::handleProductSearchResult)

            val purchaseOrderUnitsCode = productInfo.value?.purchaseOrderUnits?.code.orEmpty()
            isBlockMode.value = (purchaseOrderUnitsCode == "ST" || purchaseOrderUnitsCode == "P09") && productInfo.value?.isCountingBoxes == false
            uom.value = if (isBlockMode.value == true) {
                paramGrzMeinsPack.value = dataBase.getGrzMeinsPack().orEmpty()
                val uomInfo = zmpUtz07V001.getUomInfo(paramGrzMeinsPack.value)
                Uom(
                        code = uomInfo?.uom.orEmpty(),
                        name = uomInfo?.name.orEmpty()
                )
            } else {
                Uom(
                        code = productInfo.value?.purchaseOrderUnits?.code.orEmpty(),
                        name = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
                )
            }

            tvAccept.value = if (isBlockMode.value == false) {
                context.getString(R.string.accept_txt)
            } else {
                context.getString(
                        R.string.accept,
                        "${uom.value?.name.orEmpty()}=${productInfo.value?.nestingInOneBlock?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"
                )
            }
            suffix.value =uom.value?.name.orEmpty()

            qualityInfo.value = dataBase.getQualityInfo() ?: emptyList()
            spinQuality.value = qualityInfo.value
                    ?.map {
                        it.name
                    }
                    ?: emptyList()

            //эту строку необходимо прописывать только после того, как были установлены данные для переменных count  и suffix, а иначе фокус в поле et_count не установится
            requestFocusToCount.value = true

            if (processMarkingProductService.newProcessMarkingProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickRollback() {
        countExciseStampsScanned.value = countExciseStampsScanned.value?.minus(1)
    }

    fun onClickDetails() {
        countExciseStampsScanned.value = countExciseStampsScanned.value?.plus(1)
    }

    fun onClickAdd() {

    }

    fun onClickApply() {

    }

    fun onScanResult(data: String) {

    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            } ?: emptyList()
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

}
