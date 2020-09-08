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
    val formatterRU: SimpleDateFormat
    val formatterEN: SimpleDateFormat
    val formatterERP: SimpleDateFormat

    val productInfo: MutableLiveData<TaskProductInfo>

    val qualityInfo: MutableLiveData<List<QualityInfo>>
    val spinQualitySelectedPosition: MutableLiveData<Int>

    val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>>
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int>

    val spinProductionDate: MutableLiveData<List<String>>
    val spinProductionDateSelectedPosition: MutableLiveData<Int>

    val spinManufacturers: MutableLiveData<List<String>>
    val spinManufacturersSelectedPosition: MutableLiveData<Int>
}
