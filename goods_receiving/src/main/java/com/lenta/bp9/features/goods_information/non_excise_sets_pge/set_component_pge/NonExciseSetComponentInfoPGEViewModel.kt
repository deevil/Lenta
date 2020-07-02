package com.lenta.bp9.features.goods_information.non_excise_sets_pge.set_component_pge

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import java.text.SimpleDateFormat
import javax.inject.Inject

class NonExciseSetComponentInfoPGEViewModel : CoreViewModel(),
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val setInfo: MutableLiveData<TaskSetsInfo> = MutableLiveData()
    val planQuantityBatch: MutableLiveData<String> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val batchInfo: MutableLiveData<List<TaskBatchInfo>> = MutableLiveData()
    private val manufacturer: MutableLiveData<List<Manufacturer>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    fun getTitle() : String {
        return  "${setInfo.value!!.componentNumber.substring(setInfo.value!!.componentNumber.length - 6)} ${zfmpUtz48V001.getProductInfoByMaterial(setInfo.value!!.componentNumber)?.name}"
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
        updateDataSpinBottlingDate(position)
    }

    private fun updateDataSpinBottlingDate(position: Int) {
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(position)
        }?.code

        val bottlingDates = batchInfo.value?.filter {
            it.egais == manufactureCode
        }?.map {batch ->
            formatterRU.format(formatterEN.parse(batch.bottlingDate))
        }
        spinBottlingDateSelectedPosition.value = 0
        spinBottlingDate.value = bottlingDates

        batchInfo.value?.findLast {batch ->
            batch.egais == manufactureCode && batch.bottlingDate == formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value ?: 0)))
        }?.let {
            planQuantityBatch.value = "${it.purchaseOrderScope.toStringFormatted()} ${setInfo.value!!.uom.name}"
        }

    }

    fun onClickPositionBottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
        batchInfo.value?.let {
            planQuantityBatch.value = "${it[position].purchaseOrderScope.toStringFormatted()} ${setInfo.value!!.uom.name}"
        }
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }
}
