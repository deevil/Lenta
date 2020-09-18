package com.lenta.bp9.features.goods_information.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.base.BaseFeatures
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.map
import java.text.SimpleDateFormat
import javax.inject.Inject

abstract class BaseGoodsInfo : BaseFeatures(),
        IBaseVariables,
        IBaseCountAcceptOfProductByTaskType,
        IBaseCountRefusalOfProductByTaskType,
        IBaseCurrentManufacture,
        IBaseCurrentProductionDate,
        IBaseCurrentTypeDiscrepancies,
        IBaseProductInfo,
        IBaseQualityInfo,
        IBaseReasonRejectionInfo,
        IBaseUnit
{
    @SuppressLint("SimpleDateFormat")
    override val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)

    @SuppressLint("SimpleDateFormat")
    override val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    @SuppressLint("SimpleDateFormat")
    override val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)

    override val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    override val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    final override val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    override val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    override val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    override val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    override val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)

    final override val count: MutableLiveData<String> = MutableLiveData(DEFAULT_ENTERED_COUNT)
    override val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    override val isDefect: MutableLiveData<Boolean> by lazy {
        spinQualitySelectedPosition.map {
            if (taskType != TaskType.RecalculationCargoUnit) {
                currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
            } else {
                currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                        && currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
            }
        }
    }

    override val isNotRecountCargoUnit: MutableLiveData<Boolean> by lazy { //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
        MutableLiveData(taskType == TaskType.RecalculationCargoUnit && productInfo.value?.isWithoutRecount == true)
    }

    override val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }

    override val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    companion object {
        private const val DEFAULT_ENTERED_COUNT = "0"
        private const val DEFAULT_SPINNER_POSITION = -1
    }
}

