package com.lenta.bp9.features.goods_information.base

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.base.BaseFeatures
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import java.text.SimpleDateFormat

abstract class BaseGoodsInfo : BaseFeatures(),
        IBaseVariables,
        IBaseCountAcceptOfProductByTaskType,
        IBaseCountRefusalOfProductByTaskType,
        IBaseSpinManufacture,
        IBaseSpinProductionDate,
        IBaseTypeDiscrepanciesByTaskType,
        IBaseProductInfo,
        IBaseSpinQualityInfo,
        IBaseSpinReasonRejectionInfo,
        IBaseUnit,
        IBaseTermControl,
        IBaseSpinProcessingUnits
{
    @SuppressLint("SimpleDateFormat")
    override val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)

    @SuppressLint("SimpleDateFormat")
    override val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    @SuppressLint("SimpleDateFormat")
    override val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    override val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    override val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    final override val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    override val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    override val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    override val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val termControlType: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    override val spinTermControlSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val processingUnitsOfProduct: MutableLiveData<List<TaskProductInfo>> = MutableLiveData()
    override val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    final override val count: MutableLiveData<String> = MutableLiveData(DEFAULT_ENTERED_COUNT)
    final override val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    /**------BEGIN acceptTotalCount -----*/
    final override val acceptTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        getAcceptTotalCountByTaskType()
                    }

    private fun getAcceptTotalCountByTaskType(): Double {
        return if (taskType == TaskType.RecalculationCargoUnit) {
            getAcceptTotalCountTaskPGE()
        } else {
            getAcceptTotalCountTaskPPP()
        }
    }

    private fun getAcceptTotalCountTaskPGE(): Double {
        return if (currentTypeDiscrepanciesCodeByTaskType == TYPE_DISCREPANCIES_QUALITY_NORM
                || currentTypeDiscrepanciesCodeByTaskType == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS) {
            convertEizToBei() + countAcceptOfProductByTaskType
        } else {
            countAcceptOfProductByTaskType
        }
    }

    private fun getAcceptTotalCountTaskPPP(): Double {
        return if (currentTypeDiscrepanciesCodeByTaskType == TYPE_DISCREPANCIES_QUALITY_NORM) {
            val enteredCount = countValue.value ?: 0.0
            enteredCount + countAcceptOfProductByTaskType
        } else {
            countAcceptOfProductByTaskType
        }
    }

    override val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map { acceptTotalCountValue ->
        acceptTotalCountValue
                ?.takeIf { it > 0.0 }
                ?.let { "+ ${it.toStringFormatted()} $unitNameByTaskType" }
                ?: "0 $unitNameByTaskType"
    }
    /**------END acceptTotalCount -----*/


    /**------BEGIN refusalTotalCount -----*/
    final override val refusalTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        getRefusalTotalCountByTaskType()
                    }

    private fun getRefusalTotalCountByTaskType(): Double {
        return if (taskType == TaskType.RecalculationCargoUnit) {
            getRefusalTotalCountTaskPGE()
        } else {
            getRefusalTotalCountTaskPPP()
        }
    }

    private fun getRefusalTotalCountTaskPGE(): Double {
        return if (!(currentTypeDiscrepanciesCodeByTaskType == TYPE_DISCREPANCIES_QUALITY_NORM
                        || currentTypeDiscrepanciesCodeByTaskType == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)) {
            convertEizToBei() + countRefusalOfProductByTaskType
        } else {
            countRefusalOfProductByTaskType
        }
    }

    private fun getRefusalTotalCountTaskPPP(): Double {
        return if (currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_NORM) {
            val enteredCount = countValue.value ?: 0.0
            enteredCount + countRefusalOfProductByTaskType
        } else {
            countRefusalOfProductByTaskType
        }
    }

    override val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map { refusalTotalCountValue ->
        refusalTotalCountValue
                ?.takeIf { it > 0.0 }
                ?.let {"- ${it.toStringFormatted()} $unitNameByTaskType" }
                ?: "0 $unitNameByTaskType"
    }
    /**------END refusalTotalCount -----*/

    override val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        if (taskType == TaskType.RecalculationCargoUnit) {
            currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_NORM
                    && currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
        } else {
            currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override val isNotRecountCargoUnit: MutableLiveData<Boolean> by lazy { //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
        MutableLiveData(taskType == TaskType.RecalculationCargoUnit && productInfo.value?.isWithoutRecount == true)
    }

    override val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }

    override val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    override val isOrderUnitAndBaseUnitDifferent: MutableLiveData<Boolean> by lazy {
        //todo из ТП ПГЕ Z-партии 2.4.	Рядом с полем «Количество» отображать пиктограмму ЕИЗ/БЕИ. Пиктограмма доступна для нажатия если ЕИЗ товара не равно БЕИ при выбранной категории «Норма»
        //на некоторых экранах другая логика (см. IBaseVariables и ExciseAlcoBoxListPGEViewModel) MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
        MutableLiveData(orderUnitCode != baseUnitCode)
    }

    override val isSelectedOrderUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isOrderUnitAndBaseUnitDifferent.value ?: true)
    }

    companion object {
        private const val DEFAULT_ENTERED_COUNT = "0"
        private const val DEFAULT_SPINNER_POSITION = -1
    }
}

