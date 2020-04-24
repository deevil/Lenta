package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_card

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExciseAlcoBoxCardViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val boxInfo: MutableLiveData<TaskBoxInfo> = MutableLiveData()
    val exciseStampInfo: MutableLiveData<TaskExciseStampInfo> = MutableLiveData()
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val selectReasonRejectionCode: MutableLiveData<String> = MutableLiveData()
    val initialCount: MutableLiveData<String> = MutableLiveData()
    val enabledSpinCategorySubcategory: MutableLiveData<Boolean> = MutableLiveData()
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
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
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val scannedStampCode: MutableLiveData<String> = MutableLiveData()
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData()
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
        val countAccept = processExciseAlcoBoxAccService.taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)

        if (qualityInfo.value?.get(it!!.second)?.code == "1") {
            (it?.first ?: 0.0) + countAccept
        } else {
            countAccept
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
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
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
        if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
            (it?.first ?: 0.0) + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)

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
                "${productInfo.value?.numberBoxesControl} из ${productInfo.value?.numberStampsControl}"
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
                    "${"Значение F изначально равно 0. Увеличивать на +1 при прохождении контроля одного короба"} из ${it?.first.toString()}"
                } else {
                    "${"Значение F изначально равно 0. Увеличивать на +1 при прохождении контроля одного короба"} из ${productInfo.value?.numberBoxesControl}"
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

    val visibilityRollbackBtn: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    init {
        viewModelScope.launch {
            count.value = initialCount.value
            suffix.value = productInfo.value?.purchaseOrderUnits?.name

            if (selectReasonRejectionCode.value != null) {
                qualityInfo.value = dataBase.getQualityBoxesDefectInfo()
                enabledSpinCategorySubcategory.value = false
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
                enabledSpinCategorySubcategory.value = true
            }
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            val positionQuality = qualityInfo.value?.indexOfLast {it.code == selectQualityCode.value} ?: 0
            spinQualitySelectedPosition.value = if (positionQuality == -1) {
                0
            } else {
                positionQuality
            }
            if (selectReasonRejectionCode.value != null) {
                reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectQualityCode.value.toString())
                spinReasonRejection.value = reasonRejectionInfo.value?.map {
                    it.name
                }
                val positionReasonRejection = reasonRejectionInfo.value?.indexOfLast {it.code == selectReasonRejectionCode.value} ?: 0
                spinReasonRejectionSelectedPosition.value = if (positionReasonRejection == -1) {
                    0
                } else {
                    positionReasonRejection
                }
            }

            if (exciseStampInfo.value != null) { //значит была отсканирована марка
                val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
                    it.batchNumber == exciseStampInfo.value!!.batchNumber
                }?.egais ?: ""
                val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
                    it.code == manufacturerCode
                }?.name ?: ""
                spinManufacturers.value = listOf(manufacturerName)

                val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
                    it.batchNumber == exciseStampInfo.value!!.batchNumber
                }?.bottlingDate
                if (!dateOfPour.isNullOrEmpty()) {
                    spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
                }
            }
        }
    }

    fun onClickRollback(){
    }

    fun onClickApply() {
    }

    fun onScanResult(data: String) {
        scannedStampCode.value = data
        when (data.length) {
            68, 150 -> {

            }
            else -> ""
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            if (selectReasonRejectionCode.value != null) { //если это первый вход на экран, то в ф-ции init мы установили значение для spinReasonRejectionSelectedPosition, которое пришло
                selectReasonRejectionCode.value = null //делаем, чтобы в последствии при выборе другого qualityInfo устанавливался первый элемент из списка spinReasonRejection
            } else {
                updateDataSpinReasonRejection(qualityInfo.value!![position].code)
            }
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
