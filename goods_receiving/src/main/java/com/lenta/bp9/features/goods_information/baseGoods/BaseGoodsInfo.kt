package com.lenta.bp9.features.goods_information.baseGoods

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

abstract class BaseGoodsInfo : CoreViewModel(),
        IBaseVariables,
        IBaseCountAcceptOfProduct,
        IBaseCountRefusalOfProduct,
        IBaseCurrentManufacture,
        IBaseCurrentProductionDate,
        IBaseCurrentTypeDiscrepancies,
        IBaseDefectable,
        IBaseProductInfo,
        IBaseQualityInfo,
        IBaseReasonRejectionInfo,
        IBaseTaskManager
{
    @Inject
    override lateinit var taskManager: IReceivingTaskManager

    override val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    final override val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    override val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    override val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    override val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    override val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map { it != 0 }
    override val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    override val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    override val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    override val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
}