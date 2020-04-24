package com.lenta.bp9.features.goods_information.excise_alco_box_acc

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
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

class ExciseAlcoBoxAccInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var hyperHive: HyperHive

    private val ZfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val batchInfo: MutableLiveData<TaskBatchInfo> = productInfo.map {
        taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(it!!)
    }
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
            val countAccept = processExciseAlcoBoxAccService.getCountAcceptOfProduct() //taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + countAccept
            } else {
                countAccept
            }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = processExciseAlcoBoxAccService.getCountAcceptOfProduct() //taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
        val countRefusal = processExciseAlcoBoxAccService.getCountRefusalOfProduct() //taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
        if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
            (it?.first ?: 0.0) + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = processExciseAlcoBoxAccService.getCountRefusalOfProduct() //taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        }
    }

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount.combineLatest(spinQualitySelectedPosition).map {
        if (qualityInfo.value?.get(it?.second ?: 0)?.code == "1") {
            if ( (productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first ?: 0.0) <= 0.0) ) {
                context.getString(R.string.not_required)
            } else {
                if ((it?.first ?: 0.0) < (productInfo.value?.numberBoxesControl?.toDouble() ?: 0.0) ) {
                    "${it?.first.toStringFormatted()} из ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                } else {
                    "${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} из ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                }
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> by lazy {
        //todo https://trello.com/c/Z1SPfmAJ, 1.5. Проставлять чекбокс при прохождении контроля Y марок в Z коробах;
        MutableLiveData(false)
    }

    val tvBoxControlVal: MutableLiveData<String> = acceptTotalCount.combineLatest(spinQualitySelectedPosition).map {
        if (qualityInfo.value?.get(it?.second ?: 0)?.code == "1") {
            if ( (productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first ?: 0.0) <= 0.0) ) {
                context.getString(R.string.not_required)
            } else {
                if ((it?.first ?: 0.0) < (productInfo.value?.numberBoxesControl?.toDouble() ?: 0.0)) {
                    "${"Значение F изначально равно 0. Увеличивать на +1 при прохождении контроля одного короба"} из ${it?.first.toStringFormatted()}"
                } else {
                    "${"Значение F изначально равно 0. Увеличивать на +1 при прохождении контроля одного короба"} из ${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()}"
                }
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkBoxControl: MutableLiveData<Boolean> by lazy {
        //todo https://trello.com/c/Z1SPfmAJ, 2.4. Устанавливать чекбокс, когда F=Z;
        MutableLiveData(false)
    }

    val tvBoxListVal: MutableLiveData<String> by lazy {
        MutableLiveData(productInfo.value?.origQuantity?.toDouble().toStringFormatted())
    }

    val checkBoxList: MutableLiveData<Boolean> by lazy {
        //todo https://trello.com/c/lqyZlYQu, 1.5. Устанавливать чекбокс, когда F= Q;
        MutableLiveData(false)
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
                if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                    it!! != 0.0
                } else {
                    it!! != 0.0 && checkBoxList.value == true
                }
            }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@ExciseAlcoBoxAccInfoViewModel::viewModelScope,
                    scanResultHandler = this@ExciseAlcoBoxAccInfoViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)
            //todo временно закоментированно planQuantityBatch.value = batchInfo.value?.planQuantityBatch + " " + batchInfo.value?.uom?.name + "."
            if (processExciseAlcoBoxAccService.newProcessExciseAlcoBoxService(productInfo.value!!) == null){
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickBoxes() {
        screenNavigator.openExciseAlcoBoxListScreen(productInfo.value!!)
    }

    fun onClickDetails(){
        onScanResult("236201423037840319001M2FBU5FHOKV2MPHUVJ36QN4GUAFWRXH6ENKENR4RCWRGBN6V54IP7V6NY2L4BGJDLQZINLV3QJVEJDKMQBMMOL2OCSSLTBG2GL32XXWHRX3OPTIPPA3RZCKSNFSD7QVLY")
        //screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() : Boolean {
        return if (processExciseAlcoBoxAccService.overLimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimit()
            count.value = "0"
            false
        } else {
            if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                processExciseAlcoBoxAccService.add(acceptTotalCount.value!!.toString(), "1")
            } else {
                processExciseAlcoBoxAccService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
            }
            count.value = "0"
            true
        }
    }

    fun onClickApply() {
        if (onClickAdd()) {
            screenNavigator.goBack()
        }
    }

    fun onScanResult(data: String) {
        /**if (onClickAdd()) {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }*/

        if ( (countValue.value ?: 0.0) <= 0.0 ) {
            screenNavigator.openAlertMustEnterQuantityScreen()
            return
        }

        when (data.length) {
            68, 150 -> {
                val exciseStampInfo = processExciseAlcoBoxAccService.searchExciseStamp(data)
                if (exciseStampInfo == null) {
                    screenNavigator.openAlertScannedStampNotFoundScreen() //Отсканированная марка не числится в текущей поставке. Перейдите к коробу, в которой находится эта марка и отсканируйте ее снова.
                } else {
                    if (exciseStampInfo.materialNumber != productInfo.value!!.materialNumber) {
                        //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                        screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.materialNumber, ZfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.materialNumber)?.name ?: "")
                    } else {
                        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                            if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(productInfo.value!!.materialNumber, exciseStampInfo.boxNumber, "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = null,
                                        exciseStampInfo = exciseStampInfo,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = null,
                                        initialCount = "1")
                            }
                        } else {
                            if (checkBoxList.value == true) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = null,
                                        exciseStampInfo = exciseStampInfo,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code,
                                        initialCount = countValue.value.toStringFormatted()
                                )
                            }
                        }
                    }
                }
            }
            26 -> {
                val boxInfo = processExciseAlcoBoxAccService.searchBox(boxNumber = data)
                if (boxInfo == null) {
                    screenNavigator.openAlertScannedBoxNotFoundScreen() //Отсканированная коробка не числится в задании. Отдайте коробку поставщику.
                } else {
                    if (boxInfo.materialNumber != productInfo.value!!.materialNumber) {
                        //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                        screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = ZfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name ?: "")
                    } else {
                        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                            if (processExciseAlcoBoxAccService.getCountBoxOfProductOfDiscrepancies(productInfo.value!!.materialNumber, boxInfo.boxNumber, "1") >= acceptTotalCount.value!!.toInt()) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        exciseStampInfo = null,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = null,
                                        initialCount = "1"
                                )
                            }
                        } else {
                            if (checkBoxList.value == true) {
                                screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                            } else {
                                screenNavigator.openExciseAlcoBoxCardScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        exciseStampInfo = null,
                                        selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                        selectReasonRejectionCode = reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code,
                                        initialCount = countValue.value.toStringFormatted()
                                )
                            }
                        }
                    }
                }
            }
            else -> searchProductDelegate.searchCode(code = data, fromScan = true)
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
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
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }
}
