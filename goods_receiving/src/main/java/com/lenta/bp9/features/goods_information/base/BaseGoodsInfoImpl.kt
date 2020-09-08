package com.lenta.bp9.features.goods_information.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.base.BaseFeatures
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import java.text.SimpleDateFormat
import javax.inject.Inject

abstract class BaseGoodsInfoImpl : BaseFeatures(),
        IBaseVariables,
        IBaseCountAcceptOfProductByTaskType,
        IBaseCountRefusalOfProductByTaskType,
        IBaseCurrentManufacture,
        IBaseCurrentProductionDate,
        IBaseCurrentTypeDiscrepancies,
        IBaseDefectable,
        IBaseProductInfo,
        IBaseQualityInfo,
        IBaseReasonRejectionInfo
{
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var context: Context

    @SuppressLint("SimpleDateFormat")
    override val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)

    @SuppressLint("SimpleDateFormat")
    override val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    @SuppressLint("SimpleDateFormat")
    override val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)

    override val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    final override val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)
    override val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)
    override val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(DEFAULT_SPINNER_POSITION)
    override val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(-DEFAULT_SPINNER_POSITION)
    override val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    override val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    override val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    override val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()

    companion object {
        private const val DEFAULT_SPINNER_POSITION = -1
    }
}

