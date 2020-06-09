package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_stamp_acc_pge

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoStampAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class ExciseAlcoStampAccInfoPGEViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoStampAccPGEService: ProcessExciseAlcoStampAccPGEService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code)
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()

    val tvBottlingDate: MutableLiveData<String> by lazy {
        if (productInfo.value!!.isRus) {
            MutableLiveData(context.getString(R.string.bottling_date))
        } else {
            MutableLiveData(context.getString(R.string.date_of_entry))
        }
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val countExciseStampsScanned: MutableLiveData<Int> = MutableLiveData(0)

    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

        if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
            convertEizToBei() + countAccept
        } else {
            countAccept
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
            convertEizToBei() + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = countValue.combineLatest(spinQualitySelectedPosition).map {
        if (qualityInfo.value?.get(it?.second ?: 0)?.code == "1") {
            if ((productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first ?: 0.0) <= 0.0)) {
                checkStampControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkStampControlVisibility.value = true
                if ((it?.first ?: 0.0) < (productInfo.value?.numberBoxesControl?.toDouble()
                                ?: 0.0)) {
                    "${it?.first.toStringFormatted()} кор x ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                } else {
                    "${productInfo.value?.numberBoxesControl?.toDouble().toStringFormatted()} кор x ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
                }
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        taskManager.getReceivingTask()?.controlExciseStampsOfProduct(productInfo.value!!)
    }

    val checkBoxStampList: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //todo
        false
    }

    val tvListStampVal: MutableLiveData<String> = count.combineLatest(countExciseStampsScanned).map {
        "${it?.second} из ${it?.first}"
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        (it ?: 0) > 0
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@ExciseAlcoStampAccInfoPGEViewModel::viewModelScope,
                    scanResultHandler = this@ExciseAlcoStampAccInfoPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            count.value = count.value //todo почему-то без этой строки не выводится в tvBoxControlVal Не требуется, если включить дебаггер, то все отрабатывается, а без дебаггера пришлось дописать эту строчку

            qualityInfo.value = dataBase.getQualityInfoPGENotSurplusNotUnderload()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            if (processExciseAlcoStampAccPGEService.newProcessExciseAlcoStampPGEService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
                return@launch
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickRollback() {
        processExciseAlcoStampAccPGEService.rollbackScannedExciseStamp()
        //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
        countExciseStampsScanned.value = countExciseStampsScanned.value?.minus(1)
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {

    }

    fun onClickApply() {

    }

    fun onScanResult(data: String) {

    }

    override fun onClickPosition(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    private fun convertEizToBei(): Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }

    fun onBackPressed() {
        if (processExciseAlcoStampAccPGEService.modifications()) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {

                    }
            )
            return
        }

        screenNavigator.goBack()
    }

}
