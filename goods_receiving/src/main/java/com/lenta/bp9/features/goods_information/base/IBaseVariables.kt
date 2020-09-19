package com.lenta.bp9.features.goods_information.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import java.text.SimpleDateFormat

interface IBaseVariables {
    val productInfo: MutableLiveData<TaskProductInfo>
    val isDefect: MutableLiveData<Boolean>
    val isNotRecountCargoUnit: MutableLiveData<Boolean>
    val isGoodsAddedAsSurplus: MutableLiveData<Boolean>
    val count: MutableLiveData<String> //todo сменить название на enteredCount после того, как все ViewModel будут переведены на базовый класс
    val countValue: MutableLiveData<Double> //todo возможно тоже сменить название
    val acceptTotalCount: MutableLiveData<Double> //todo сменить название на acceptTotalCountByTaskType после того, как все ViewModel будут переведены на базовый класс
    val acceptTotalCountWithUom: MutableLiveData<String> //todo сменить название на acceptTotalCountWithUnitByTaskType после того, как все ViewModel будут переведены на базовый класс
    val refusalTotalCount: MutableLiveData<Double> //todo сменить название на refusalTotalCountByTaskType после того, как все ViewModel будут переведены на базовый класс
    val refusalTotalCountWithUom: MutableLiveData<String> //todo сменить название на refusalTotalCountWithUnitByTaskType после того, как все ViewModel будут переведены на базовый класс
    val isSelectedOrderUnit: MutableLiveData<Boolean>
    val isOrderUnitAndBaseUnitDifferent: MutableLiveData<Boolean>
    val isDiscrepancy: MutableLiveData<Boolean> //todo сменить название на isMovedFromScreenDiscrepanciesFound после того, как все ViewModel будут переведены на базовый класс

    val formatterRU: SimpleDateFormat
    val formatterEN: SimpleDateFormat
    val formatterERP: SimpleDateFormat

    val qualityInfo: MutableLiveData<List<QualityInfo>>
    val spinQualitySelectedPosition: MutableLiveData<Int>

    val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>>
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int>

    val spinProductionDate: MutableLiveData<List<String>>
    val spinProductionDateSelectedPosition: MutableLiveData<Int>

    val spinManufacturers: MutableLiveData<List<String>>
    val spinManufacturersSelectedPosition: MutableLiveData<Int>

    val termControlType: MutableLiveData<List<QualityInfo>>
    val spinTermControlSelectedPosition: MutableLiveData<Int>

    val processingUnitsOfProduct: MutableLiveData<List<TaskProductInfo>>
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int>
}
