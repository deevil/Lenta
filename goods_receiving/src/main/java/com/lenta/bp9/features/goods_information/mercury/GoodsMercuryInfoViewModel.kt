package com.lenta.bp9.features.goods_information.mercury

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsMercuryInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = MutableLiveData(10.0) /**by lazy  {
        countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
            } else {
                0.0
            }
        }
    }*/

    val refusalTotalCount: MutableLiveData<Double> = MutableLiveData(70.0) /** by lazy  {
        countValue.
                combineLatest(spinReasonRejectionSelectedPosition).
                combineLatest(spinQualitySelectedPosition).
                map{
                    if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                        (it?.first?.first ?: 0.0) + taskManager.
                                getReceivingTask()!!.
                                taskRepository.
                                getProductsDiscrepancies().
                                getCountRefusalOfProductOfReasonRejection(productInfo.value!!, reasonRejectionInfo.value?.get(it?.first?.second ?: 0) ?.code)
                    } else {
                        0.0
                    }
                }
    }*/

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
                it!! != 0.0
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            spinManufacturers.value = listOf("Manufacturer 1", "Manufacturer 2", "Manufacturer 3")

        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    fun onClickPositionSpinManufacturers(position: Int){
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinProductionDate(position: Int){
        spinProductionDateSelectedPosition.value = position
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

    fun onClickDetails(){
        return
    }

    fun onClickAdd() {
        return
    }

    fun onClickApply() {
        return
    }

    fun onScanResult(data: String) {
        return
    }
}
