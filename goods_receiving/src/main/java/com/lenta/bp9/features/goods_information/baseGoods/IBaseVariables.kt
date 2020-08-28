package com.lenta.bp9.features.goods_information.baseGoods

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo

interface IBaseVariables {
    val taskManager: IReceivingTaskManager
    val dataBase: IDataBaseRepo
    val productInfo: MutableLiveData<TaskProductInfo>
    val qualityInfo: MutableLiveData<List<QualityInfo>>
    val spinQualitySelectedPosition: MutableLiveData<Int>
    val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>>
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int>
    val spinProductionDateSelectedPosition: MutableLiveData<Int>
    val spinManufacturersSelectedPosition: MutableLiveData<Int>
    val spinManufacturers: MutableLiveData<List<String>>
    val spinProductionDate: MutableLiveData<List<String>>
}
